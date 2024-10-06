package com.donut.chapter.questionChatBot;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ChatHistoryMapper {
    List<ChatHistoryDTO> getChatHistoryByChapterId(String chapterId);

    int saveQuestionAndAnswer(Map<String, String> chatMap);

    List<ChatHistoryDTO> getChatHistoryByDate(String date);

//    @Select("SELECT * FROM CHAT_HISTORY ORDER BY CREATE_DATE ASC")
    List<ChatHistoryDTO> getAllChatHistories();
}
