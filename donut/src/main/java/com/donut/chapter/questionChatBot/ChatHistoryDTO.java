package com.donut.chapter.questionChatBot;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ChatHistoryDTO implements Serializable {
    private int id;
    private int chapterId;
    private String content;
    private boolean isUser;
    private LocalDateTime createDate;

}
