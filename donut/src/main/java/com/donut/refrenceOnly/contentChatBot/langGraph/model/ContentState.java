package com.donut.refrenceOnly.contentChatBot.langGraph.model;

import org.bsc.langgraph4j.state.AgentState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ContentState extends AgentState {
    public ContentState(Map<String, Object> initData) {
        super(initData);
    }
    public String userQuery(){
        Optional<String> result = value("userQuery");
        return result.orElse("non user query");
    }


    public List<String> searchQuery(){
        Optional<List<String>> result = value("searchQuery");
        return result.orElse(new ArrayList<>());
    }
    public List<Map<String, Object>> documents(){
        Optional<List<Map<String, Object>>> result = value("documents");
        return result.orElse(new ArrayList<>());
    }


    public List<String> imageQuery(){
        Optional<List<String>> result = value("imageQuery");
        return result.orElse(new ArrayList<>());
    }

    public String genHTML(){
        Optional<String> result = value("genHTML");
        return result.orElse("null");
    }
}
