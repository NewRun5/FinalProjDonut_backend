package com.donut.curriculum.langGraph;

import com.donut.curriculum.langGraph.model.ChatHistory;
import com.donut.curriculum.langGraph.model.UserNeeds;
import com.donut.curriculum.langGraph.model.Curriculum;
import com.donut.curriculum.langGraph.model.CurriculumState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.utils.CollectionsUtils.mapOf;

@RequiredArgsConstructor
@Component
public class CurriculumLangGraph {
    private final CurriculumLangGraphService service;

    public StateGraph<CurriculumState> buildGraph() throws Exception {
        return new StateGraph<>(CurriculumState::new)
                .addNode("needMoreInput", node_async(this::needMoreInput))
                .addNode("generateSearchQuery", node_async(this::generateSearchQuery))
                .addNode("generateCurriculumUseRag", node_async(this::generateCurriculumUseRag))
                .addNode("generateCurriculumNotUseRag", node_async(this::generateCurriculumNotUseRag))
                .addNode("isEnoughUserInput", node_async(this::isEnoughUserInput))
                .addEdge(START, "isEnoughUserInput")
                .addConditionalEdges("isEnoughUserInput", edge_async(this::checkNeedsNull)
                        , mapOf(
                                "null", "needMoreInput",
                                "not null", "generateSearchQuery"
                        ))
                .addEdge("needMoreInput", END)
                .addConditionalEdges("generateSearchQuery", edge_async(this::isUsefulData)
                        , mapOf(
                                "true", "generateCurriculumUseRag",
                                "false", "generateCurriculumNotUseRag"
                        ))
                .addEdge("generateCurriculumUseRag", END)
                .addEdge("generateCurriculumNotUseRag", END);
    }

    private Map<String, Object> isEnoughUserInput(CurriculumState state) {
        List<ChatHistory> chatHistoryList = state.chatHistoryList();
        UserNeeds userNeeds = service.isEnoughUserInput(chatHistoryList);
        return mapOf("userNeeds", userNeeds);
    }

    private String checkNeedsNull(CurriculumState state) {
        UserNeeds userNeeds = state.userNeeds();
        System.out.println(userNeeds);
        if (userNeeds.getTopic() == null) {
            return "null";
        }
        if (userNeeds.getLevel() == null) {
            return "null";
        }
        if (userNeeds.getProficiency() == null) {
            return "null";
        }
        return "not null";
    }

    private Map<String, Object> needMoreInput(CurriculumState state) {
        List<ChatHistory> chatHistoryList = state.chatHistoryList();
        UserNeeds userNeeds = state.userNeeds();
        String result = service.needMoreInput(chatHistoryList, userNeeds);
        return mapOf("generatedMessage", result);
    }

    private Map<String, Object> generateSearchQuery(CurriculumState state) {
        List<ChatHistory> chatHistoryList = state.chatHistoryList();
        List<String> searchQueryList = service.generateSearchQuery(chatHistoryList);
        System.out.println("생성된 검색어 수 : " + searchQueryList.size());
        List<Map<String, Object>> searchResult = new ArrayList<>();
        for (String query : searchQueryList) {
            List<Map<String, Object>> result = service.searchDocument(query);
            searchResult.addAll(result);
        }
        return mapOf("documentList", searchResult);
    }

    private String isUsefulData(CurriculumState state) {
        List<Map<String, Object>> documents = state.documentList();
        List<ChatHistory> chatHistoryList = state.chatHistoryList();
        System.out.println("검색된 문서" + documents);
        System.out.println("채팅 내역" + chatHistoryList);
        String result = service.isUsefulData(documents, chatHistoryList);
        System.out.println("정보 유용함 여부 : " + result);
        return result;
    }

    private Map<String, Object> generateCurriculumUseRag(CurriculumState state) {
        List<Map<String, Object>> documents = state.documentList();
        List<ChatHistory> chatHistoryList = state.chatHistoryList();
        Curriculum curriculum = service.generateCurriculumUseRag(documents, chatHistoryList);
        return mapOf("generatedCurriculum", curriculum, "isRagUsed", true);
    }
    private Map<String, Object> generateCurriculumNotUseRag(CurriculumState state){
        List<ChatHistory> chatHistoryList = state.chatHistoryList();
        Curriculum curriculum = service.generateCurriculumNotUseRag(chatHistoryList);
        return mapOf("generatedCurriculum", curriculum, "isRagUsed", false);
    }
}
