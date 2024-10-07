package com.donut.chapter.questionChatBot.langGraph;

import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;
import java.util.Optional;

public class QuestionState extends AgentState {
    public QuestionState(Map<String, Object> initData) {
        super(initData);
    }
    public String question(){
        Optional<String> result = value("question");
        return result.orElseThrow(() -> new IllegalStateException( "question is not set!" ));
    }
}
