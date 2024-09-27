package com.donut.chapter.questionChatBot;
import com.donut.chapter.questionChatBot.langGraph.MRS;
import com.donut.chapter.questionChatBot.langGraph.QuestionState;
import com.donut.common.gson.JsonUtil;
import com.donut.common.utils.ChatBotComponent;
import com.donut.common.utils.ChatBotMemory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.bsc.langgraph4j.utils.CollectionsUtils.mapOf;

@Slf4j  // 로그를 기록하기 위한 Lombok 어노테이션
@Component("questionHandler")
@RequiredArgsConstructor
public class QuestionChatBotHandler extends TextWebSocketHandler {
    /* 시스템 메세지 */
    private final String sysMsg = "당신은 사용자의 질문을 답변해주는 챗봇입니다. ";
    /* api 를 편하게 사용 가능한 컴포넌트 */
    private final ChatBotComponent chatBotComponent;
    /* 세션마다 메모리를 만들기 위한 객체 */
    private final Map<String, ChatBotMemory> memoryMap = new ConcurrentHashMap<>();
    private final Map<String, String> chapterIdMap = new ConcurrentHashMap<>();
    /* 로직을 위임하기 위한 service 객체 */
    private final ChatHistoryService service;
    /* JSON 파싱용 유틸 */
    private final JsonUtil jsonUtil;

    /* LangGraph를 실행할 QuestionLangGraph 주입 */
    private final MRS questionLangGraph;  // 의존성 주입


    /**
     * 웹소켓이 연결된 직후 실행되는 함수입니다.
     * uri 를 바탕으로 채팅 내역을 조회하여 json.stringify 형태로 반환하는 함수입니다.
     *
     * @param session : 클라이언트와 공유하는 세션
     * @throws Exception : sendMessage 메소드에 대한 예외처리
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        /* 세션 ID 가져오기 */
        String sessionId = session.getId();

        // URI의 쿼리 파라미터가 존재하는지 확인
        String query = session.getUri().getQuery();
        int chapterId = 1; // 기본값으로 1 설정 (정수값으로 설정)

        if (query != null && query.contains("chapterId=")) {
            try {
                chapterId = Integer.parseInt(query.split("chapterId=")[1]); // 올바른 정수 값으로 변환
            } catch (NumberFormatException e) {
                log.warn("Invalid chapterId format in query. Default chapterId will be used.");
            }
        } else {
            log.warn("No chapterId found in the WebSocket query. Default chapterId will be used.");
        }

        /* 챕터 아이디를 바탕으로 대화 내역을 가져오고 memory 객체 만들기 */
        List<ChatHistoryDTO> chatHistoryList = service.getChatHistoryByChapterId(String.valueOf(chapterId));
        ChatBotMemory chatBotMemory = new ChatBotMemory(sysMsg);
        chatHistoryList.forEach(chatHistory -> {
            if (chatHistory.isUser()) {
                chatBotMemory.save(new UserMessage(chatHistory.getContent()));
            } else {
                chatBotMemory.save(new AssistantMessage(chatHistory.getContent()));
            }
        });

        /* 세션 맵에 담아두기 */
        /* 세션 맵에 담아두기 */
        memoryMap.put(sessionId, chatBotMemory);
        chapterIdMap.put(sessionId, String.valueOf(chapterId));

        /* 저장된 대화내역 객체 반환 */
        session.sendMessage(new TextMessage(jsonUtil.jsonStringify(chatHistoryList)));
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();
        ChatBotMemory memory = memoryMap.get(sessionId);
        String chapterId = chapterIdMap.get(sessionId);

        String request = message.getPayload();
        log.info("Received message: " + request);

        // LangGraph 실행 부분입니다.
        try {
            // 실제 질문을 기반으로 검색 쿼리 생성
            String searchQuery = chatBotComponent.getChatResponse("Generate search query from question: " + request).getContent();

            // 무조건 'rag_search'를 설정
            String searchType = "rag_search";

            // LangGraph에 필요한 초기 데이터를 설정합니다.
            Map<String, Object> initData = mapOf(
                    "question", request,          // 실제 사용자의 질문
                    "search_query", searchQuery,  // 생성된 검색 쿼리
                    "search_type", searchType     // 검색 유형을 무조건 'rag_search'로 설정
            );
            QuestionState state = new QuestionState(initData); // QuestionState 객체 생성
            log.info("LangGraph starting from START node...");
            questionLangGraph.getApp().stream(state.data());
            log.info("LangGraph processed state: " + state.data());

            // LangGraph에서 생성된 응답을 가져옵니다.
            String langGraphResponse = state.<String>value("generation").orElse("LangGraph did not generate a response.");
            log.info("LangGraph response: " + langGraphResponse);

            // LangGraph에서 응답이 생성되었을 경우
            if (!"LangGraph did not generate a response.".equals(langGraphResponse)) {
                int maxLength = 200;
                if (langGraphResponse.length() > maxLength) {
                    langGraphResponse = langGraphResponse.substring(0, maxLength);
                    log.warn("LangGraph response truncated to " + maxLength + " characters.");
                }

                // LangGraph 응답을 DB에 저장
                Map<String, String> chatMap = mapOf(
                        "request", request,
                        "response", langGraphResponse,
                        "chapterId", chapterId);
                int result = service.saveQuestionAndAnswer(chatMap);
                log.info("Chat history saved to DB, result: " + result);

                // LangGraph 응답을 클라이언트에 전송
                session.sendMessage(new TextMessage(langGraphResponse));
                return;
            }
        } catch (Exception e) {
            log.error("LangGraph execution error: ", e);
        }

        // LangGraph 실행이 실패하거나 응답이 없을 경우 기본 챗봇 응답 생성
        try {
            String response = chatBotComponent.getChatResponseByMemory(memory, request).getContent();
            int maxLength = 200;
            if (response.length() > maxLength) {
                response = response.substring(0, maxLength);
                log.warn("Response truncated to " + maxLength + " characters.");
            }

            // 응답을 DB에 저장
            Map<String, String> chatMap = mapOf(
                    "request", request,
                    "response", response,
                    "chapterId", chapterId);
            int result = service.saveQuestionAndAnswer(chatMap);
            log.info("Chat history saved to DB, result: " + result);

            session.sendMessage(new TextMessage(response));
        } catch (Exception e) {
            log.error("Error generating chatbot response: ", e);
            session.sendMessage(new TextMessage("An error occurred while processing your request."));
        }
    }


}
//        /* 해당 세션의 메모리 객체 가져오기 */
//        String sessionId = session.getId();
//        ChatBotMemory memory = memoryMap.get(sessionId);
//        String chapterId = chapterIdMap.get(sessionId);
//
//        /* 받은 텍스트 추출 및 출력 */
//        String request = message.getPayload();
//        /* 챗봇 컴포넌트 활용해서 답변 생성.
//         * 랭그래프는 이곳에 적용하시면 됩니다. */
//        String response = chatBotComponent.getChatResponseByMemory(memory, request).getContent();
