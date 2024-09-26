package com.donut.curriculum;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@ToString
public class CurriculumDTO {
//    private int id;
//    private String userId;
//    private String description;
//    private String imagePath;
//    private double progress;
//    private LocalDate completeDate;
//    private LocalDate deleteDate;
    @JsonPropertyDescription("커리큘럼의 단원 리스트입니다.")
    private String unitList;
}
