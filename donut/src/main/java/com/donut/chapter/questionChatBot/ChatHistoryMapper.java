package com.donut.chapter.questionChatBot;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ChatHistoryMapper {
    List<ChatHistoryDTO> getChatHistoryByChapterId(String chapterId);

    int saveQuestionAndAnswer(Map<String, String> chatMap);
}
