package com.donut.curriculum.langGraph.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Setter
@Getter
@ToString
public class UserNeeds implements Serializable {
    @JsonPropertyDescription("사용자가 공부하고싶어하는 주제입니다 어느정도 자세하면 좋습니다.")
    private String topic;
    @JsonPropertyDescription("사용자가 알고 있는 현재 배경 지식입니다.")
    private String backgroundInformation;
    @JsonPropertyDescription("사용자가 원하는 난이도입니다. 예를들어 초급, 중급, 고급, 중학교, 고등학교 등이 있습니다.")
    private String level;
}
