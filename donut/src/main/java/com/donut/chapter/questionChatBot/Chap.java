package com.donut.chapter.questionChatBot;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class Chap {
    private int id;
    private String title;
    private String userId;
    private LocalDate createDate;
    private List<ChatHistoryDTO> chatList;
}
