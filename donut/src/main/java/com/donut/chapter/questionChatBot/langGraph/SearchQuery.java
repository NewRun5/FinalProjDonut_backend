package com.donut.chapter.questionChatBot.langGraph;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SearchQuery implements Serializable {
    @JsonPropertyDescription("문서 검색어 리스트입니다.")
    private List<String> documentSearchQueryList;
    @JsonPropertyDescription("이미지 검색어 리스트입니다.")
    private List<String> imageSearchQueryList;
}
