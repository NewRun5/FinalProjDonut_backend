package com.donut.curriculum.langGraph;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CurriculumNeedData {
    @JsonPropertyDescription("유저가 공부싶어하는 주제입니다")
    private String topic;
    @JsonPropertyDescription("유저가 원하는 난이도입니다.")
    private String level;
}
