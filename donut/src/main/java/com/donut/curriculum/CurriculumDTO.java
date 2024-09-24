package com.donut.curriculum;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Setter
@Getter
@ToString
public class CurriculumDTO {
    private int id;
    private String userId;
    private String description;
    private String imagePath;
    private double progress;
    private LocalDate completeDate;
    private LocalDate deleteDate;
}
