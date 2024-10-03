package com.donut.chapter;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Data
public class ChapterDTO {
    private Integer id;
    private String title;
    private String goal;
    private String description;
    private String content;
    private String summaryNote;
    private LocalDate completeDate;
}
