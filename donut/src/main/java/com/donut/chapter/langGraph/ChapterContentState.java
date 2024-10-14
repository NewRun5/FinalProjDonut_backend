package com.donut.chapter.langGraph;

import com.donut.chapter.ChapterDTO;
import com.donut.chapter.langGraph.model.SearchQueries;
import com.donut.chapter.langGraph.model.SelfFeedback;
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
    public String beforeContent(){
        Optional<String> result = value("beforeContent");
        return result.orElse(null);
    }
    public SearchQueries searchQueries(){
        Optional<SearchQueries> result = value("searchQueries");
        return result.orElse(null);
    }
    public List<Map<String, Object>> documentList(){
        Optional<List<Map<String, Object>>> result = value("documentList");
        return result.orElse(new ArrayList<>());
    }
    public List<Map<String, Object>> imageList(){
        Optional<List<Map<String, Object>>> result = value("imageList");
        return result.orElse(new ArrayList<>());
    }
    public SelfFeedback selfFeedback(){
        Optional<SelfFeedback> result = value("selfFeedback");
        return result.orElse(null);
    }
    public String genContent(){
        Optional<String> result = value("genContent");
        return result.orElse(null);
    }
}
