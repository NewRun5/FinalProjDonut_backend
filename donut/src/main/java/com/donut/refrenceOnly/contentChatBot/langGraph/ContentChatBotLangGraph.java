package com.donut.refrenceOnly.contentChatBot.langGraph;

import com.donut.refrenceOnly.contentChatBot.langGraph.model.ContentState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.utils.CollectionsUtils.mapOf;

@RequiredArgsConstructor
@Component
public class ContentChatBotLangGraph {
    private final ContentService service;

    public StateGraph<ContentState> buildGraph() throws Exception{
        return new StateGraph<>(ContentState::new)
                .addNode("genSearchQuery", node_async(this::genSearchQuery))
                .addNode("hybridSearchQuery", node_async(this::hybridSearchQuery))
                .addNode("genHTML", node_async(this::genHTML))
                .addEdge(START, "genSearchQuery")
                .addEdge("genSearchQuery", "hybridSearchQuery")
                .addEdge("hybridSearchQuery", "genHTML")
                .addEdge("genHTML", END);
    }




    private Map<String, Object> genSearchQuery(ContentState state){
        System.out.println(1);
        String userQuery = state.userQuery();
        List<String> searchQueryList = service.genSearchQuery(userQuery);
        return mapOf("searchQuery", searchQueryList);
    }
    private Map<String, Object> hybridSearchQuery(ContentState state){
        List<String> queryList = state.searchQuery();
        System.out.println("쿼리 리스트: " + queryList);
        List<Map<String, Object>> documents = service.hybridSearchQuery(queryList);
        System.out.println("완료");
        return mapOf("documents", documents);
    }
    private Map<String, Object> genHTML(ContentState state){
        List<Map<String, Object>> documents = state.documents();
        String userQuery = state.userQuery();
        String HTMLCode = service.genHTML(documents, userQuery);
        System.out.println(HTMLCode);
        return mapOf("genHTML", HTMLCode);
    }
}
