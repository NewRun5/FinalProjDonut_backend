package com.donut.curriculum.langGraph.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

import java.io.Serializable;
@Data
public class InputComment implements Serializable {
    @JsonPropertyDescription("유저에게 정보를 수집하기 위한 대화입니다. 자연스러운 말투로 작성하세요.")
    private String comment;
    @JsonPropertyDescription("100점 만점에 80점 이상이면 통과입니다.")
    private int collectScore;
}
