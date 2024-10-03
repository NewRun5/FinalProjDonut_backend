package com.donut.curriculum;

import com.donut.chapter.ChapterDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Data
@Setter
public class CurriculumDTO {
    private Integer id;
    private String userId;
    private String title;
    private String description;
    private String imagePath;
    private Float progress;
    private LocalDate createDate;
    private LocalDate completeDate;
    private LocalDate deleteDate;
    private List<ChapterDTO> chapterList;
    private boolean isRagUsed;
}
