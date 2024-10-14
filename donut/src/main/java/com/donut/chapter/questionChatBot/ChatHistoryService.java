package com.donut.chapter.questionChatBot;

import com.donut.common.utils.ChatBotComponent;
import com.donut.common.utils.ChatBotMemory;
import com.donut.common.utils.PromptLoader;
import com.donut.curriculum.langGraph.model.Chapter;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatHistoryService {
    private final ChatHistoryMapper mapper;
    private final ChatBotComponent chatBotComponent;
    private final PromptLoader promptLoader = new PromptLoader("chat/");

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

    public int insertNewChapter(String userId, List<ChatHistoryDTO> chatHistory) {
        String prompt = promptLoader.get("genTitle");
        ChatBotMemory memory = ChatBotMemory.from(chatHistory);
        memory.getHistory().add(0, new SystemMessage(prompt));

        String chapterTitle = chatBotComponent.getChatResponseByMemory(memory).getContent();
        LocalDate current = LocalDate.now();
        Chap chap = new Chap();
        chap.setTitle(chapterTitle);
        chap.setCreateDate(current);
        chap.setUserId(userId);
        mapper.insertChapterByUserId(chap);
        return chap.getId();
    }
}
