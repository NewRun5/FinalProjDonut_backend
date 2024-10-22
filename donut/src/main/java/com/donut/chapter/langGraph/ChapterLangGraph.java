package com.donut.chapter.langGraph;

import com.donut.chapter.ChapterDTO;
import com.donut.chapter.langGraph.model.SearchQueries;
import com.donut.chapter.langGraph.model.SelfFeedback;
import com.donut.common.search.DocumentSearchService;
import com.donut.common.search.ImageSearchService;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.utils.CollectionsUtils.mapOf;

@Component
@RequiredArgsConstructor
public class ChapterLangGraph {
    private final ChapterLangGraphService service;
    private final ImageSearchService imageSearchService;
    private final DocumentSearchService documentSearchService;

    public StateGraph<ChapterContentState> buildGraph() throws Exception{
        return new StateGraph<>(ChapterContentState::new)
                .addNode("genSearchQueries", node_async(this::genSearchQueries))
                .addNode("hybridSearch", node_async(this::hybridSearch))
                .addNode("genContent", node_async(this::genContent))
                .addNode("selfTest", node_async(this::selfTest))
                .addEdge(START, "genSearchQueries")
                .addEdge("genSearchQueries", "hybridSearch")
                .addEdge("hybridSearch", "genContent")
                .addEdge("genContent", "selfTest")
                .addConditionalEdges("selfTest", edge_async(this::testScore),
                        mapOf("true", END,
                                "false", "genContent"));

    }

    private String testScore(ChapterContentState state) {
        SelfFeedback feedback = state.selfFeedback();
        if(feedback.getScore() >= 90){
            return "true";
        } else {
            return "false";
        }
    }

    private Map<String, Object> genSearchQueries(ChapterContentState state) {
        ChapterDTO chapter = state.chapter();
        SearchQueries searchQueries = service.genSearchQueries(chapter);
        return mapOf("searchQueries", searchQueries);
    }
    private Map<String, Object> hybridSearch(ChapterContentState state) {

        SearchQueries searchQueries = state.searchQueries();
        List<Map<String, Object>> documentSearchResult = service.searchDocument(searchQueries.getDocumentSearchQuery());
        List<Map<String, Object>> imageSearchResult = service.searchImage(searchQueries.getDocumentSearchQuery());

        return mapOf("documentList", documentSearchResult,
                "imageList", imageSearchResult);
    }

    private Map<String, Object> genContent(ChapterContentState state) {
        List<Map<String, Object>> documentSearchResult = state.documentList();
        List<Map<String, Object>> imageSearchResult = state.imageList();
        ChapterDTO chapter = state.chapter();
        String beforeContent = state.beforeContent();
        SelfFeedback feedback = state.selfFeedback();
        String genContent;
        if(feedback == null){
            genContent = service.genContent(chapter, beforeContent, documentSearchResult, imageSearchResult);
        } else {
            String content = state.genContent();
            genContent = service.genContentByFeedback(chapter, beforeContent, documentSearchResult, imageSearchResult, content, feedback);
        }
        return Map.of("genContent", genContent);
    }

    private Map<String, Object> selfTest(ChapterContentState state) {
        String genContent = state.genContent();
        String beforeContent = state.beforeContent();
        ChapterDTO chapter = state.chapter();
        SelfFeedback selfFeedback = service.selfTest(genContent, beforeContent, chapter);
        return mapOf("selfFeedback", selfFeedback);
    }
}