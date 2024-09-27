package com.donut.curriculum.langGraph;

import org.bsc.langgraph4j.state.AgentState;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CurriculumState extends AgentState {
    public CurriculumState(Map<String, Object> initData) {
        super(initData);
    }
    public String userQuery(){
        Optional<String> result = value("userQuery");
        return result.orElseThrow(() -> new IllegalStateException( "유저 쿼리 누락" ));
    }
    public CurriculumNeedData needData(){
        Optional<CurriculumNeedData> result = value("needData");
        return result.orElseThrow(()->new IllegalStateException("커리큘럼 생성에 필요한 데이터 누락"));
    }
    public List<String> documentList(){
        Optional<List<String>> result = value("documentList");
        return result.orElseThrow(()->new IllegalStateException("문서 데이터 누락"));
    }
}
