package com.donut.chapter.questionChatBot;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Setter
@Getter
@ToString
public class ChatHistoryDTO {
    private int id;
    private int chapterId;
    private String content;
    private boolean isUser;
    private LocalDateTime createDate;
}
