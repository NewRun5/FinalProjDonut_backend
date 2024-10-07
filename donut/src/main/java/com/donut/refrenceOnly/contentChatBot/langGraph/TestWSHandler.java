package com.donut.refrenceOnly.contentChatBot.langGraph;

import com.donut.chapter.questionChatBot.ChatHistoryService;
import com.donut.common.utils.ChatBotMemory;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

import static org.bsc.langgraph4j.utils.CollectionsUtils.mapOf;

@Component
@RequiredArgsConstructor
public class TestWSHandler extends TextWebSocketHandler {

    private final Map<String, ChatBotMemory> sessions = new HashMap<>();
    private final String sysMsg = "당신은 사용자가 궁금해하는 정보의 문서를 만드는 어시스턴트입니다.";
    private final ContentChatBotLangGraph langGraph;
    private final ChatHistoryService chatHistoryService; // ChatHistoryService 주입

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        ChatBotMemory chatBotMemory = new ChatBotMemory(sysMsg);
        sessions.put(session.getId(), chatBotMemory);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload(); // 사용자가 입력한 질문 (userQuery)
        ChatBotMemory memory = sessions.get(session.getId());
        memory.save(new UserMessage(payload));

        String generation = ""; // 챗봇의 응답
        var graph = langGraph.buildGraph().compile().stream(mapOf("userQuery", payload));
        for (var r : graph) {
            System.out.printf("Node: '%s':\n", r.node());
            generation = r.state().genHTML(); // 생성된 HTML 응답
        }

        memory.save(new AssistantMessage(generation));
        session.sendMessage(new TextMessage(generation));

        // 질문과 답변을 저장하기 위한 로직 추가
        Map<String, String> chatMap = new HashMap<>();
        chatMap.put("chapterId", "1"); // 예시로 챕터 ID를 지정합니다. 실제로는 동적으로 처리 필요
        chatMap.put("request", payload); // 사용자가 입력한 질문
        chatMap.put("response", generation); // 챗봇의 대답

        chatHistoryService.saveQuestionAndAnswer(chatMap); // 질문과 대답을 저장
    }
}
