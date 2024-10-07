package com.donut.chapter.langGraph;

import com.donut.chapter.ChapterDTO;
import org.bsc.langgraph4j.state.AgentState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ChapterContentState extends AgentState {
    public ChapterContentState(Map<String, Object> initData) {
        super(initData);
    }
    public ChapterDTO chapter(){
        Optional<ChapterDTO> result = value("chapter");
        return result.orElse(null);
    }
    public String[] searchQuery(){
        Optional<String[]> result = value("searchQuery");
        return result.orElse(new String[0]);
    }
    public List<Map<String, Object>> documentList(){
        Optional<List<Map<String, Object>>> result = value("documentList");
        return result.orElse(new ArrayList<>());
    }
    public String[] searchImgQuery(){
        Optional<String[]> result = value("searchImgQuery");
        return result.orElse(new String[0]);
    }
    public String contentPrototype(){
        Optional<String> result = value("contentPrototype");
        return result.orElse(null);
    }
    public String content(){
        Optional<String> result = value("content");
        return result.orElse(null);
    }
    public List<Map<String, Object>> imageDataList(){
        Optional<List<Map<String, Object>>> result = value("imageDataList");
        return result.orElse(null);
    }
}
