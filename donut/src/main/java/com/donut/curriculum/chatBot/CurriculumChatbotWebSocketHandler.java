package com.donut.curriculum.chatBot;

import com.donut.common.utils.ChatBotComponent;
import com.donut.common.utils.ChatBotMemory;
import com.donut.curriculum.CurriculumDTO;
import com.donut.curriculum.langGraph.CurriculumLangGraph;
import com.donut.curriculum.langGraph.mentoringGraph.MRS;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.bsc.langgraph4j.utils.CollectionsUtils.mapOf;

@Component("curriculumHandler")
@RequiredArgsConstructor
public class CurriculumChatbotWebSocketHandler extends TextWebSocketHandler {
    /* 병렬처리가 가능한 Map. 각각 세션의 아이디를 Key로 세션을 관리할 수 있다. */
    private final Map<String, ChatBotMemory> sessions = new ConcurrentHashMap<>();
    private final ChatBotComponent component;
    private final String sysMsg = "당신은 사용자가 원하는 커리큘럼을 생성해주는 챗봇입니다. 주어진 json 스키마를 벗어나는 커리큘럼은 만들면 안됩니다.";
    private final MRS mrs;
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
        /* 챗봇 컴포넌트 활용해서 답변 생성 */
        var result = mrs.buildGraph().stream(mapOf("question", payload));
        String generation = "";
        for( var r : result ) {
            System.out.printf( "Node: '%s':\n", r.node() );

            generation = r.state().generation().orElse( "")
            ;
        }

        System.out.println( generation );


        session.sendMessage(new TextMessage(generation));
    }
}
