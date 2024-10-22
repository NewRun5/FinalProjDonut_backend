package com.donut.chapter.langGraph.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

import java.io.Serializable;

@Data
public class SelfFeedback implements Serializable {
    @JsonPropertyDescription("조건에 대한 코멘트입니다.")
    private String comment;
    @JsonPropertyDescription("조건에 얼마나 만족하는지에 대한 점수입니다. 100점 만점에 85점 이상이면 통과입니다.")
    private int score;
}
