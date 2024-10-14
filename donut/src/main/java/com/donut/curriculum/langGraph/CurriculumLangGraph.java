package com.donut.curriculum.langGraph;

import com.donut.curriculum.langGraph.model.InputComment;
import com.donut.curriculum.langGraph.model.SerializableMemory;
import com.donut.curriculum.langGraph.model.GenCurriculum;
import com.donut.curriculum.langGraph.model.CurriculumState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.utils.CollectionsUtils.mapOf;

@RequiredArgsConstructor
@Component
public class CurriculumLangGraph {
    private final CurriculumLangGraphService service;
    public StateGraph<CurriculumState> buildGraph() throws Exception {
        return new StateGraph<CurriculumState>(CurriculumState::new)
                .addNode("clearHistory", node_async(this::clearHistory))
                .addNode("genInputComment", node_async(this::genInputComment))
                .addNode("hybridSearch", node_async(this::hybridSearch))
                .addNode("genCurriculum", node_async(this::genCurriculum))
                .addNode("fixCurriculum", node_async(this::fixCurriculum))
                .addNode("temp", node_async(this::temp))
                .addConditionalEdges(START, edge_async(this::isGenCurriculum), mapOf(
                        "true",  "temp",
                        "false", "genInputComment"
                ))
                .addConditionalEdges("temp", edge_async(this::isWantNewCurriculum), mapOf(
                        "true", "clearHistory",
                        "false", "fixCurriculum"
                ))
                .addEdge("fixCurriculum", END)
                .addEdge("clearHistory", "genInputComment")
                .addConditionalEdges("genInputComment", edge_async(this::isEnoughUserInput), mapOf(
                        "true", "hybridSearch",
                        "false", END
                ))
                .addEdge("hybridSearch", "genCurriculum")
                .addEdge("genCurriculum", END);
    }

    private Map<String, Object> genCurriculum(CurriculumState curriculumState) {
        List<Map<String, Object>> documentList = curriculumState.documentList();
        List<SerializableMemory> chatHistory = curriculumState.chatHistory();
        GenCurriculum genCurriculum = service.genCurriculum(documentList, chatHistory);
        return mapOf("genCurriculum", genCurriculum);
    }

    private Map<String, Object> hybridSearch(CurriculumState curriculumState) {
        List<SerializableMemory> chatHistory = curriculumState.chatHistory();
        List<Map<String, Object>> documentList = service.hybridSearch(chatHistory);
        return mapOf("documentList", documentList);
    }

    private String isEnoughUserInput(CurriculumState curriculumState) {
        InputComment inputComment = curriculumState.inputComment();
        if(inputComment.getCollectScore() > 80) {
            return "true";
        } else {
            return "false";
        }
    }

    private Map<String, Object> genInputComment(CurriculumState curriculumState) {
        List<SerializableMemory> chatHistory = curriculumState.chatHistory();
        InputComment inputComment = service.genInputComment(chatHistory);
        return mapOf("inputComment", inputComment);
    }

    private Map<String, Object> fixCurriculum(CurriculumState curriculumState) {
        List<SerializableMemory> chatHistory = curriculumState.chatHistory();
        GenCurriculum result = service.fixCurriculum(chatHistory);
        return mapOf("genCurriculum", result);
    }

    private Map<String, Object> clearHistory(CurriculumState curriculumState) {
        List<SerializableMemory> chatHistory = curriculumState.chatHistory();
        if (!chatHistory.isEmpty()) {
            SerializableMemory lastAdded = chatHistory.get(chatHistory.size() - 1); // 가장 최근에 추가된 요소
            chatHistory.clear(); // 전체 리스트 초기화
            chatHistory.add(lastAdded); // 마지막 요소만 다시 추가
        }
        return mapOf("chatHistory", chatHistory);
    }

    private Map<String, Object> temp(CurriculumState curriculumState) {
        return mapOf();
    }

    private String isWantNewCurriculum(CurriculumState curriculumState) {
        List<SerializableMemory> memory = curriculumState.chatHistory();
        String isWantNewCurriculum = service.isWantNewCurriculum(memory);
        return isWantNewCurriculum;
    }

    private String isGenCurriculum(CurriculumState curriculumState) {
        GenCurriculum curriculum = curriculumState.genCurriculum();
        if(curriculum.getTitle() == null) return "false";
        return "true";
    }
}
