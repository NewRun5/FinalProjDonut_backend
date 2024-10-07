package com.donut.curriculum;

import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface CurriculumMapper {
    int saveCurriculum(CurriculumDTO request);

    void saveChapters(Map<String, Object> paramMap);

    CurriculumDTO getCurriculumById(Integer id);

    int saveImagePath(Map<String, String> imagePath);
}
