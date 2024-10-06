package com.donut.chapter.questionChatBot;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ChatHistoryController {

  private final ChatHistoryService chatHistoryService;

  // 날짜를 기준으로 대화 기록을 가져오는 API
  @GetMapping("/api/chat-history")
  public List<ChatHistoryDTO> getChatHistoryByDate(@RequestParam String date) {
    return chatHistoryService.getChatHistoryByDate(date);
  }

  // chapterId를 기준으로 대화 기록을 가져오는 API
  @GetMapping("/api/chat-history/chapter")
  public List<ChatHistoryDTO> getChatHistoryByChapterId(@RequestParam String chapterId) {
    return chatHistoryService.getChatHistoryByChapterId(chapterId);
  }

  // 대화 내용을 저장하는 API
  @PostMapping("/api/chat-history")
  public int saveQuestionAndAnswer(@RequestBody Map<String, String> chatMap) {
    return chatHistoryService.saveQuestionAndAnswer(chatMap);
  }

  @QueryMapping
  public List<ChatHistoryDTO> getAllChatHistories() {
    return chatHistoryService.getAllChatHistories();
  }
}

