package com.donut.curriculum.chatBot;

import com.donut.common.gson.JsonUtil;
import com.donut.curriculum.langGraph.CurriculumLangGraph;
import com.donut.common.utils.ChatBotComponent;
import com.donut.curriculum.langGraph.model.GenCurriculum;
import com.donut.curriculum.langGraph.model.InputComment;
import com.donut.curriculum.langGraph.model.Sender;
import com.donut.curriculum.langGraph.model.SerializableMemory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.bsc.langgraph4j.utils.CollectionsUtils.mapOf;

@Component("curriculumHandler")
@RequiredArgsConstructor
public class CurriculumChatbotWebSocketHandler extends TextWebSocketHandler {
    /* 병렬처리가 가능한 Map. 각각 세션의 아이디를 Key로 세션을 관리할 수 있다. */
    private final CurriculumLangGraph langGraph;
    private final JsonUtil jsonUtil;

    /* 세션 */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Map<String, Object> sessionMap =  session.getAttributes();
        List<SerializableMemory> chatHistory = new ArrayList<>();
        sessionMap.put("genCurriculum", new GenCurriculum());
        sessionMap.put("chatHistory", chatHistory);
    }
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        /* 받은 텍스트 추출 및 출력 */
        String payload = message.getPayload();
        List<SerializableMemory> chatHistory = (List<SerializableMemory>) session.getAttributes().get("chatHistory");
        chatHistory.add(new SerializableMemory(Sender.USER, payload));
        GenCurriculum genCurriculum = (GenCurriculum) session.getAttributes().get("genCurriculum");
        var graph = langGraph.buildGraph().compile().stream(mapOf("chatHistory", chatHistory, "genCurriculum", genCurriculum));
        GenCurriculum graphCurriculum = null;
        InputComment inputComment = null;
        for (var a : graph){
            System.out.println(a.node());
            graphCurriculum = a.state().genCurriculum();
            inputComment = a.state().inputComment();
            chatHistory = a.state().chatHistory();
        }
        if(graphCurriculum.getTitle() == null){
            graphCurriculum = new GenCurriculum();
            System.out.println(inputComment.getCollectScore());
            graphCurriculum.setComment(inputComment.getComment());
            chatHistory.add(new SerializableMemory(Sender.ASSISTANT, inputComment.getComment()));
        } else {
            chatHistory.add(new SerializableMemory(Sender.ASSISTANT, jsonUtil.jsonStringify(graphCurriculum)));
        }

        session.getAttributes().put("chatHistory", chatHistory);

        String response = jsonUtil.jsonStringify(graphCurriculum);

        session.sendMessage(new TextMessage(response));
    }
}
