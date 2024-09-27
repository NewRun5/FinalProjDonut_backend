package com.donut.chapter.questionChatBot.langGraph;

import com.donut.common.search.MongoSearchService;
import com.donut.common.utils.ChatBotComponent;
import com.google.gson.Gson;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.utils.CollectionsUtils.mapOf;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionLangGraph {

    private final ChatBotComponent component;  // OpenAI 챗봇 컴포넌트
    private final MongoSearchService mongoService;  // MongoDB에서 데이터를 검색하는 서비스
    private final Gson gson;  // JSON 변환을 위한 Gson 라이브러리
    private final double THRESHOLD = 0.01;  // 검색 결과의 최소 점수 임계값
    private final int MAX_SEARCH_COUNT = 3;  // 최대 검색 시도 횟수

    CompiledGraph<QuestionState> app = null;  // LangGraph로 생성된 그래프 객체

    // LangGraph를 초기화하는 메서드
    @PostConstruct
    public void init() throws Exception {
        log.info("Initializing LangGraph...");
        try {
            app = this.buildGraph();
            log.info("LangGraph initialized successfully");
        } catch (Exception e) {
            log.error("Error initializing LangGraph: ", e);
        }
    }

    // 생성된 LangGraph 객체를 반환하는 메서드
    public CompiledGraph<QuestionState> getApp() {
        return app;
    }

    // LangGraph를 빌드하고 상태 그래프와 노드, 엣지 및 조건을 정의함
    public CompiledGraph<QuestionState> buildGraph() throws GraphStateException {
        log.info("Building LangGraph...");
        return new StateGraph<>(QuestionState::new)

                .addEdge(START, "query_search")

                // "query_search" 노드를 추가하여 검색 수행
                .addNode("query_search", node_async(state -> {
                    log.info("Entering query_search node");
                    // state에서 search_query 값을 추출하여 로그로 출력
                    String searchQuery = state.search_query();
                    log.info("Performing query_search with query: " + searchQuery);

                    return query_search(searchQuery); // 검색에 search_query 값을 전달
                }))

                // "check_search_result" 노드를 추가하여 검색 결과 확인
                .addNode("check_search_result", node_async(state -> {
                    log.info("Entering check_search_result node");
                    return check_search_result(state);
                }))

                // "generate_answer" 노드를 추가하여 답변 생성
                .addNode("generate_answer", node_async(state -> {
                    log.info("Entering generate_answer node");
                    return generate_answer(state);
                }))

                // "query_search"에서 "check_search_result"로 연결
                .addEdge("query_search", "check_search_result")

                // "check_search_result"에서 조건부로 다음 노드를 결정
                .addConditionalEdges("check_search_result", edge_async(state -> {
                    String result = condition(state).toString();
                    log.info("Result from check_search_result: " + result);
                    return result;
                }), mapOf(
                        "not_enough", "query_search",  // 검색 결과가 충분하지 않으면 다시 검색 시도
                        "enough", "generate_answer"  // 충분하면 답변 생성 단계로 이동
                ))

                // "generate_answer"에서 종료("END")로 연결
                .addEdge("generate_answer", END)
                .compile();  // 그래프 컴파일
    }

    // "query_search" 노드: 검색 쿼리를 수행하고 MongoDB에서 결과를 가져옴
    private Map<String, Object> query_search(String searchQuery) {
        log.info("Performing query_search with query: " + searchQuery);

        // MongoDB에서 하이브리드 검색 수행
        List<Map<String, Object>> documents = mongoService.hybridSearch(searchQuery);
        log.info("Number of documents found: " + documents.size());

        // 검색 시도 횟수를 1로 설정 (필요에 따라 조정)
        int search_count = 1;

        if (documents.isEmpty()) {
            log.warn("No documents found for query: " + searchQuery);
        }

        // 검색 결과와 검색 시도 횟수를 반환
        return mapOf("documents", documents, "search_count", search_count, "search_query", searchQuery);
    }

    private Map<String, Object> check_search_result(QuestionState state) {
        List<Map<String, Object>> documents = state.documents();
        int search_count = state.search_count();

        log.info("Checking search result, number of documents: " + documents.size());

        // 검색 결과가 없거나 첫 번째 문서의 점수가 임계값보다 낮으면 재검색을 시도
        if (documents.isEmpty() || !documents.get(0).containsKey("score")) {
            if (search_count < MAX_SEARCH_COUNT) {
                log.warn("No valid score found or documents are empty, retrying search...");
                return mapOf("result", "not_enough");
            }
        }

        // 첫 번째 문서의 점수가 임계값 이상인지 확인
        if (!documents.isEmpty() && (double) documents.get(0).get("score") >= THRESHOLD) {
            log.info("Search result is enough with score: " + documents.get(0).get("score"));
            return mapOf("result", "enough");
        }

        // 최대 검색 시도 횟수를 초과한 경우
        log.warn("Maximum search count reached or no valid score found.");
        return mapOf("result", "enough");
    }



    // "generate_answer" 노드: 검색 결과를 바탕으로 OpenAI API를 통해 답변을 생성
    private Map<String, Object> generate_answer(QuestionState state) {
        String question = state.question();  // 상태에서 질문 가져옴
        List<Map<String, Object>> documents = state.documents();  // 상태에서 검색된 문서 목록을 가져옴

        log.info("Generating answer for question: " + question);

        String strDocuments = gson.toJson(documents);  // 검색된 문서 목록을 JSON 형식으로 변환
        String generation;
        try {
            // OpenAI 챗봇 API를 호출하여 답변 생성
            generation = component.getChatResponseWithSysMsg(
                    "You are an assistant for question-answering tasks\r\n ### context ###\r\n"
                            + strDocuments, question).getContent();
        } catch (Exception e) {
            log.error("Error generating response from OpenAI: ", e);
            generation = "Sorry, I couldn't generate an answer at this time.";
        }

        log.info("Generated answer: " + generation);
        return mapOf("generation", generation);  // 생성된 답변 반환
    }

    // 검색 조건을 평가하는 메서드: 검색 결과의 충분성 여부를 확인하고 조건을 반환
    private Map<String, Object> condition(QuestionState state) {
        List<Map<String, Object>> documents = state.documents();  // 상태에서 문서 목록 가져옴
        int search_count = state.search_count();  // 상태에서 검색 시도 횟수를 가져옴

        // 검색된 문서가 없거나, 첫 번째 문서의 점수가 임계값보다 낮으면
        if (documents.isEmpty() || (double) documents.get(0).get("score") < THRESHOLD) {
            if (search_count < MAX_SEARCH_COUNT) {  // 검색 시도 횟수가 최대 횟수를 넘지 않은 경우
                log.info("Condition: not enough search results, continuing search");
                return mapOf("result", "not_enough");  // "not_enough" 결과 반환
            }
        }
        log.info("Condition: enough search results");
        return mapOf("result", "enough");  // 충분한 검색 결과일 경우 "enough" 결과 반환
    }
}
