package com.donut.curriculum.langGraph;

import com.donut.common.gson.JsonUtil;
import com.donut.common.utils.ChatBotComponent;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static org.bsc.langgraph4j.utils.CollectionsUtils.mapOf;

@RequiredArgsConstructor
public class CurriculumLangGraph {
    private final JsonUtil jsonUtil;
    private final ChatBotComponent component;
    private Map<String, Object> parseNeedData(CurriculumState state){
        String userQuery = state.userQuery();
        CurriculumNeedData data = (CurriculumNeedData) component.getStructuredOutput("당신은 입력받은 데이터를 토대로 객체를 만들어내는 봇입니다." +
                "알고있는 json 형태 외의 다른 형태의 대답을 해선 안됩니다." +
                "모르는 필드는 null 로 반환합니다.", userQuery, CurriculumNeedData.class);
        return mapOf("needData", data);
    }
    private String needDataNullCheck(CurriculumState state){
        if(state.needData().getTopic() == null){
            System.out.println("데이터수집이 더 필요합니다.");
            return "needMoreData";
        }
        if(state.needData().getLevel() == null){
            System.out.println("데이터수집이 더 필요합니다.");
            return "needMoreData";
        }
        System.out.println("데이터가 충분합니다.");
        return "enoughData";
    }
}
