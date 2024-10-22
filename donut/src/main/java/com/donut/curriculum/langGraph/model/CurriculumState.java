package com.donut.curriculum.langGraph.model;

import org.bsc.langgraph4j.state.AgentState;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CurriculumState extends AgentState {
    public CurriculumState(Map<String, Object> initData) {
        super(initData);
    }

    public GenCurriculum genCurriculum(){
        Optional<GenCurriculum> result = value("genCurriculum");
        return result.orElse(null);
    }
    public List<SerializableMemory> chatHistory(){
        Optional<List<SerializableMemory>> result = value("chatHistory");
        return result.orElse(null);
    }
    public List<Map<String, Object>> documentList(){
        Optional<List<Map<String, Object>>> result = value("documentList");
        return result.orElse(null);
    }
    public InputComment inputComment(){
        Optional<InputComment> result = value("inputComment");
        return result.orElse(null);
    }
}
