package com.donut.chapter.questionChatBot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatHistoryService {
    private final ChatHistoryMapper mapper;
    public List<ChatHistoryDTO> getChatHistoryByChapterId(String chapterId) {
        return mapper.getChatHistoryByChapterId(chapterId);
    }

    public int saveQuestionAndAnswer(Map<String, String> chatMap) {
        return mapper.saveQuestionAndAnswer(chatMap);
    }
}
