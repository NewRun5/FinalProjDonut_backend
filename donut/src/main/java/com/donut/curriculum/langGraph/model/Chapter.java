package com.donut.curriculum.langGraph.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class Chapter implements Serializable {
    @JsonPropertyDescription("챕터의 제목입니다.")
    private String title;
    @JsonPropertyDescription("학습 목표입니다.")
    private String goal;
    @JsonPropertyDescription("챕터에 대한 설명입니다.")
    private String description;
}
