package com.donut.curriculum;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.Arguments;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CurriculumController {
    private final CurriculumService service;
    private final HttpSession session;
    @MutationMapping
    public Integer saveCurriculum(@Argument("input") CurriculumDTO request){
        Integer response = service.saveCurriculum(request);
        return response;
    }
    @QueryMapping
    public CurriculumDTO getCurriculumById(@Argument("id") Integer id){
        CurriculumDTO result = service.getCurriculumById(id);
        return result;
    }
    @QueryMapping
    public List<CurriculumDTO> getCurriculumListBySession(){
        String userId = (String) session.getAttribute("user");
        System.out.println(userId);
        List<CurriculumDTO> result = service.getCurriculumListByUserId(userId);
        return result;
    }
}
