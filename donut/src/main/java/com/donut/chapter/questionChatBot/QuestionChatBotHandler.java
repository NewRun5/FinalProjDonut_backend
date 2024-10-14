package com.donut.chapter.questionChatBot;

import com.donut.chapter.questionChatBot.langGraph.QuestionLangGraph;
import com.donut.common.gson.JsonUtil;
import com.donut.common.utils.ChatBotComponent;
import com.donut.common.utils.ChatBotMemory;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static org.bsc.langgraph4j.utils.CollectionsUtils.mapOf;

@Component("questionHandler")
@RequiredArgsConstructor
public class QuestionChatBotHandler extends TextWebSocketHandler {
    /* 시스템 메세지 */
    private final String sysMsg = "당신은 사용자의 질문을 답변해주는 챗봇입니다. ";
    /* api 를 편하게 사용 가능한 컴포넌트 */
    private final ChatBotComponent chatBotComponent;
    /* 로직을 위임하기 위한 service 객체 */
    private final ChatHistoryService service;
    /* JSON 파싱용 유틸 */
    private final JsonUtil jsonUtil;
    private final QuestionLangGraph langGraph;

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
        /* 현재 채팅중인 챕터 정보 URI 에서 가져오기 */
        String chapterId = (String) session.getAttributes().get("chapterId");
        System.out.println(chapterId);
        List<ChatHistoryDTO> chatHistoryList;
        if (chapterId.equals("none")) {
            chatHistoryList = new ArrayList<>();
        } else {
            chatHistoryList = service.getChatHistoryByChapterId(chapterId);
        }
        session.getAttributes().put("chatHistory", chatHistoryList);

        session.sendMessage(new TextMessage(jsonUtil.jsonStringify(chatHistoryList)));
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        /* 받은 텍스트 추출 및 출력 */
        String request = message.getPayload();
        String chapterId = session.getAttributes().get("chapterId").toString();
        List<ChatHistoryDTO> chatHistoryList = (List<ChatHistoryDTO>) session.getAttributes().get("chatHistory");
        String userId = (String) session.getAttributes().get("user");
        ChatHistoryDTO requestChat = new ChatHistoryDTO();
        requestChat.setContent(request);
        requestChat.setUser(true);
        chatHistoryList.add(requestChat);
        if (chapterId.equals("none")) {
            chapterId = service.insertNewChapter(userId, chatHistoryList) + "";
            System.out.println(chapterId);
            session.getAttributes().put("chapterId", chapterId);
        }
        ChatHistoryDTO userInput = new ChatHistoryDTO();
        userInput.setUser(true);
        userInput.setContent(request);
        chatHistoryList.add(userInput);
        var graph = langGraph.buildGraph().compile().stream(mapOf("chatHistory", chatHistoryList));
        String result = "";
        for (var v : graph) {
            result = v.state().genAnswer();
        }
        /* DB 에 저장하는 로직 */
        Map<String, String> chatMap = mapOf(
                "request", request,
                "response", result,
                "chapterId", chapterId);
        int insertResult = service.saveQuestionAndAnswer(chatMap);
        /* 생성된 답변 추출하여 반환 */
        session.sendMessage(new TextMessage(result));
    }
}
