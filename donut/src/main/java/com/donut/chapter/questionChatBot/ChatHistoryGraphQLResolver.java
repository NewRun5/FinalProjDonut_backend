package com.donut.chapter.questionChatBot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.graphql.data.method.annotation.QueryMapping;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatHistoryGraphQLResolver {

  private final ChatHistoryService chatHistoryService;

  @QueryMapping
  public List<ChatHistoryDTO> getChatHistoryByChapterId(int chapterId) {
    return chatHistoryService.getChatHistoryByChapterId(String.valueOf(chapterId));
  }

  @QueryMapping
  public List<ChatHistoryDTO> getChatHistoryByChapterId(int chapterId, String fromDate, String toDate) {
    return chatHistoryService.getChatHistoryByChapterId(chapterId, fromDate, toDate);
  }
}

