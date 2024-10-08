package com.donut.chapter;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class ChapterController {
    private final ChapterService service;
    @QueryMapping
    public String getChapterContentById(@Argument int chapterId){
        String content = service.getChapterContentById(chapterId);
        return content;
    }
    @MutationMapping
    public LocalDate updateCompleteDate(@Argument int chapterId){
        LocalDate completeDate = service.updateCompleteDate(chapterId);
        return completeDate;
    }
}
