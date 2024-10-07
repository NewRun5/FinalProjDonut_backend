package com.donut.chapter.questionChatBot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatHistoryService {
    private final ChatHistoryMapper mapper;
    public List<ChatHistoryDTO> getChatHistoryByChapterId(String chapterId) {
        return mapper.getChatHistoryByChapterId(chapterId);
    }

    public int saveQuestionAndAnswer(Map<String, String> chatMap) {
        return mapper.saveQuestionAndAnswer(chatMap);
    }

    public List<ChatHistoryDTO> getChatHistoryByDate(String date) {
        return mapper.getChatHistoryByDate(date);
    }

    public List<ChatHistoryDTO> getAllChatHistories() {
        List<ChatHistoryDTO> chatHistories = mapper.getAllChatHistories();
        System.out.println("Fetched chat histories: " + chatHistories);  // 로깅 추가
        return chatHistories;
    }

    public List<ChatHistoryDTO> getChatHistoryByChapterId(int chapterId, String fromDate, String toDate) {
        return mapper.getChatHistoryByChapterIdWithDate(chapterId, fromDate, toDate);
    }
}
