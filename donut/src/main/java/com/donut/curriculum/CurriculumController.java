package com.donut.curriculum;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.Arguments;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class CurriculumController {
    private final CurriculumService service;
    @MutationMapping
    public Integer saveCurriculum(@Argument("input") CurriculumDTO request){
        System.out.println("인서트");
        Integer response = service.saveCurriculum(request);
        return response;
    }
    @QueryMapping
    public CurriculumDTO getCurriculumById(@Argument("id") Integer id){
        CurriculumDTO result = service.getCurriculumById(id);
        return result;
    }
}
