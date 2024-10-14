package com.donut.chapter.questionChatBot.langGraph;

import com.donut.chapter.questionChatBot.ChatHistoryDTO;
import org.bsc.langgraph4j.state.AgentState;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class QuestionState extends AgentState {
    public QuestionState(Map<String, Object> initData) {
        super(initData);
    }

    public SearchQuery searchQuery(){
        Optional<SearchQuery> result = value("searchQuery");
        return result.orElse(null);
    }
    public List<ChatHistoryDTO> chatHistory(){
        Optional<List<ChatHistoryDTO>> chatHistory = value("chatHistory");
        return chatHistory.orElse(null);
    }
    public List<Map<String, Object>> documentList(){
        Optional<List<Map<String, Object>>> documentList = value("documentList");
        return documentList.orElse(null);
    }
    public List<Map<String, Object>> imageList(){
        Optional<List<Map<String, Object>>> imageList = value("imageList");
        return imageList.orElse(null);
    }
    public String genAnswer(){
        Optional<String> result = value("genAnswer");
        return result.orElse(null);
    }
}
