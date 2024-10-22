package com.donut.chapter.questionChatBot.langGraph;

import com.donut.chapter.questionChatBot.ChatHistoryDTO;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.utils.CollectionsUtils.mapOf;

@Component
@RequiredArgsConstructor
public class QuestionLangGraph {
    private final QuestionLangGraphService service;
    public StateGraph<QuestionState> buildGraph() throws Exception{
        return new StateGraph<QuestionState>(QuestionState::new)
                .addNode("genSearchQuery", node_async(this::genSearchQuery))
                .addNode("hybridSearch", node_async(this::hybridSearch))
                .addNode("genAnswer", node_async(this::genAnswer))
                .addEdge(START, "genSearchQuery")
                .addEdge("genSearchQuery", "hybridSearch")
                .addEdge("hybridSearch", "genAnswer")
                .addEdge("genAnswer", END);
    }
    private Map<String, Object> genSearchQuery(QuestionState questionState) {
        List<ChatHistoryDTO> chatHistoryList = questionState.chatHistory();
        SearchQuery searchQuery = service.genSearchQuery(chatHistoryList);
        return mapOf("searchQuery", searchQuery);
    }

    private Map<String, Object> hybridSearch(QuestionState questionState) {
        SearchQuery searchQuery = questionState.searchQuery();
        List<Map<String, Object>> documentResult = service.documentSearch(searchQuery.getDocumentSearchQueryList());
        List<Map<String, Object>> imageResult = service.imageSearch(searchQuery.getImageSearchQueryList());
        return mapOf("documentList", documentResult, "imageList", imageResult);
    }
    
    private Map<String, Object> genAnswer(QuestionState questionState) {
        List<Map<String, Object>> documentResult = questionState.documentList();
        List<Map<String, Object>> imageResult = questionState.imageList();
        List<ChatHistoryDTO> chatHistoryList = questionState.chatHistory();
        String genAnswer = service.genAnswer(chatHistoryList, documentResult, imageResult);
        return mapOf("genAnswer", genAnswer);
    }
}
