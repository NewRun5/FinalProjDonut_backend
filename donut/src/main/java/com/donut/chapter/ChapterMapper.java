package com.donut.chapter;

import org.apache.ibatis.annotations.Mapper;

import java.io.Serializable;
import java.util.Map;

@Mapper
public interface ChapterMapper {
    ChapterDTO getChapterById(int chapterId);

    int updateChapterContent(Map<String,? extends Serializable> chapterId);

    int updateCompleteDate(Map<String,? extends Comparable<? extends Comparable<?>>> currentTime);
}
