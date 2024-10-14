package com.donut.chapter.langGraph.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SearchQueries implements Serializable {
    @JsonPropertyDescription("문서를 검색하기 위한 검색어 리스트입니다.")
    private List<String> documentSearchQuery;
    @JsonPropertyDescription("이미지를 검색하기 위한 검색어 리스트입니다.")
    private List<String> imageSearchQuery;
}
