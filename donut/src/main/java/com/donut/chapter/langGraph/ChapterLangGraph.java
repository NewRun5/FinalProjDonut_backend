package com.donut.chapter.langGraph;

import com.donut.chapter.ChapterDTO;
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
public class ChapterLangGraph {
    private final ChapterLangGraphService service;
    public StateGraph<ChapterContentState> buildGraph() throws Exception{
        return new StateGraph<>(ChapterContentState::new)
                .addNode("genDocumentSearchQuery", node_async(this::genDocumentSearchQuery))
                .addNode("documentSearch", node_async(this::documentSearch))
                .addNode("genContentPrototype", node_async(this::genContentPrototype))
                .addNode("genImageSearchQuery", node_async(this::genImageSearchQuery))
                .addNode("imageSearch", node_async(this::imageSearch))
                .addNode("genContent", node_async(this::genContent))
                .addEdge(START, "genDocumentSearchQuery")
                .addEdge("genDocumentSearchQuery", "documentSearch")
                .addEdge("documentSearch", "genContentPrototype")
                .addEdge("genContentPrototype", END);
//                .addEdge("genImageSearchQuery", "imageSearch")
//                .addEdge("imageSearch", "genContent")
//                .addEdge("genContent", END);
    }

    private Map<String, Object> genDocumentSearchQuery(ChapterContentState state) {
        ChapterDTO chapter = state.chapter();
        String[] searchQuery = service.genDocumentSearchQuery(chapter);
        return mapOf("searchQuery", searchQuery);
    }

    private Map<String, Object> documentSearch(ChapterContentState state) {
        String[] searchQuery = state.searchQuery();
        List<Map<String, Object>> documentList = service.documentSearch(searchQuery);
        return mapOf("documentList", documentList);
    }

    private Map<String, Object> genContentPrototype(ChapterContentState state) {
        List<Map<String, Object>> documentList = state.documentList();
        ChapterDTO chapter = state.chapter();
        String contentPrototype = service.genContentPrototype(documentList, chapter);
        return mapOf("contentPrototype", contentPrototype);
    }

    private Map<String, Object> genImageSearchQuery(ChapterContentState state) {
        String contentPrototype = state.contentPrototype();
        String[] searchImgQuery = service.genSearchImgQuery(contentPrototype);
        return mapOf("searchImgQuery", searchImgQuery);
    }

    private Map<String, Object> imageSearch(ChapterContentState state) {
        return null;
    }

    private Map<String, Object> genContent(ChapterContentState state) {
        return null;
    }
}