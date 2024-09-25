package com.donut.chapter.questionChatBot;

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
    /* 세션마다 메모리를 만들기 위한 객체 */
    private final Map<String, ChatBotMemory> memoryMap = new ConcurrentHashMap<>();
    private final Map<String, String> chapterIdMap = new ConcurrentHashMap<>();
    /* 로직을 위임하기 위한 service 객체 */
    private final ChatHistoryService service;
    /* JSON 파싱용 유틸 */
    private final JsonUtil jsonUtil;

    /**
     * 웹소켓이 연결된 직후 실행되는 함수입니다.
     * uri 를 바탕으로 채팅 내역을 조회하여 json.stringify 형태로 반환하는 함수입니다.
     * @param session : 클라이언트와 공유하는 세션
     * @throws Exception : sendMessage 메소드에 대한 예외처리
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        /* 세션 ID 가져오기 */
        String sessionId = session.getId();
        /* 현재 채팅중인 챕터 정보 URI 에서 가져오기 */
        String chapterId = Objects.requireNonNull(session.getUri()).getQuery().split("chapterId=")[1];
        /* 챕터 아이디를 바탕으로 대화 내역을 가져오고 memory 객체 만들기 */
        List<ChatHistoryDTO> chatHistoryList = service.getChatHistoryByChapterId(chapterId);
        ChatBotMemory chatBotMemory = new ChatBotMemory(sysMsg);
        chatHistoryList.forEach(chatHistory -> {
            if (chatHistory.isUser()) {
                chatBotMemory.save(new UserMessage(chatHistory.getContent()));
            } else {
                chatBotMemory.save(new AssistantMessage(chatHistory.getContent()));
            }
        });


        /* 세션 맵에 담아두기 */
        memoryMap.put(sessionId, chatBotMemory);
        chapterIdMap.put(sessionId, chapterId);

        /* 저장된 대화내역 객체 반환 */
        session.sendMessage(new TextMessage(jsonUtil.jsonStringify(chatHistoryList)));
    }
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        /* 해당 세션의 메모리 객체 가져오기 */
        String sessionId = session.getId();
        ChatBotMemory memory = memoryMap.get(sessionId);
        String chapterId = chapterIdMap.get(sessionId);

        /* 받은 텍스트 추출 및 출력 */
        String request = message.getPayload();
        /* 챗봇 컴포넌트 활용해서 답변 생성.
        * 랭그래프는 이곳에 적용하시면 됩니다. */
        String response = chatBotComponent.getChatResponse(memory, request).getContent();
        Map<String, String> chatMap = mapOf(
                "request", request,
                "response", response,
                "chapterId", chapterId);
        int result = service.saveQuestionAndAnswer(chatMap);
        /* 생성된 답변 추출하여 반환 */
        session.sendMessage(new TextMessage(response));
    }
}
