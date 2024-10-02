package com.donut.curriculum.langGraph.model;

import org.bsc.langgraph4j.state.AgentState;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CurriculumState extends AgentState {
    public CurriculumState(Map<String, Object> initData) {
        super(initData);
    }

    public UserNeeds userNeeds(){
        Optional<UserNeeds> result = value("userNeeds");
        return result.orElse(new UserNeeds());
    }
    public List<ChatHistory> chatHistoryList(){
        Optional<List<ChatHistory>> result = value("chatHistory");
        return result.orElse(null);
    }
    public List<Map<String, Object>> documentList(){
        Optional<List<Map<String, Object>>> result = value("documentList");
        return result.orElse(null);
    }

    public Curriculum generatedCurriculum(){
        Optional<Curriculum> result = value("generatedCurriculum");
        return result.orElse(null);
    }
    public String generatedMessage(){
        Optional<String> result = value("generatedMessage");
        return result.orElse(null);
    }
    public boolean isRagUsed(){
        Optional<Boolean> result = value("isRagUsed");
        return result.orElse(false);
    }
}
