package com.donut.chapter;

import com.donut.chapter.questionChatBot.Chap;
import com.donut.curriculum.CurriculumDTO;
import org.apache.ibatis.annotations.Mapper;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Mapper
public interface ChapterMapper {
    ChapterDTO getChapterById(int chapterId);

    int updateChapterContent(Map<String,? extends Serializable> chapterId);

    int updateCompleteDate(Map<String,? extends Comparable<? extends Comparable<?>>> currentTime);

    CurriculumDTO getCurriculumByChapterId(int chapterId);

    int updateCreateDate(Map<String,? extends Comparable<? extends Comparable<?>>> currentTime);

    List<Chap> getAllChapterByUserId(String userId);
}
