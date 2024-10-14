package com.donut.curriculum.langGraph.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class GenCurriculum implements Serializable {
    @JsonPropertyDescription("커리큘럼의 제목입니다.")
    private String title;
    @JsonPropertyDescription("소제목입니다.")
    private String description;
    @JsonPropertyDescription("커리큘럼의 챕터 리스트입니다.")
    private List<Chapter> chapterList;
    @JsonPropertyDescription("만들어진 커리큘럼에 대한 당신의 코멘트입니다.")
    private String comment;
}
