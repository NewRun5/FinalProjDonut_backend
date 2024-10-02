package com.donut.refrenceOnly.contentChatBot.langGraph;

import com.donut.common.utils.ChatBotMemory;
import com.donut.curriculum.langGraph.model.ChatHistory;
import com.donut.curriculum.langGraph.model.Curriculum;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.bsc.langgraph4j.utils.CollectionsUtils.mapOf;

@Component
@RequiredArgsConstructor
public class TestWSHandler extends TextWebSocketHandler {
    /* 병렬처리가 가능한 Map. 각각 세션의 아이디를 Key로 세션을 관리할 수 있다. */
    private final Map<String, ChatBotMemory> sessions = new ConcurrentHashMap<>();
    private final String sysMsg = "당신은 사용자가 궁금해하는 정보의 문서를 만드는 어시스턴트입니다.";
    private final ContentChatBotLangGraph langGraph;
    /* 세션 */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        ChatBotMemory chatBotMemory = new ChatBotMemory(sysMsg);
        sessions.put(session.getId(), chatBotMemory);
    }
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        /* 받은 텍스트 추출 및 출력 */
        String payload = message.getPayload();
        ChatBotMemory memory = sessions.get(session.getId());
        memory.save(new UserMessage(payload));


        String generation = "";
        var graph = langGraph.buildGraph().compile().stream(mapOf("userQuery", payload));
        for( var r : graph ) {
            System.out.printf( "Node: '%s':\n", r.node() );
            generation = r.state().genHTML();
        }

        memory.save(new AssistantMessage(generation));

        session.sendMessage(new TextMessage(generation));
    }
}
