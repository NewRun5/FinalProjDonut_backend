package com.donut.common.utils;

import lombok.Getter;
import lombok.ToString;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatBotMemory {
    private final Integer limit;
    private final String sysMsg;
    @Getter
    private final List<Message> history;

    public ChatBotMemory(int limit, String sysMsg) {
        this.limit = limit * 2 + 1;
        this.sysMsg = sysMsg;
        List<Message> history = new ArrayList<Message>();
        history.add(new SystemMessage(sysMsg));
        this.history =  history;
    }
    public ChatBotMemory(int limit) {
        this.limit = limit * 2;
        this.sysMsg = null;
        this.history = new ArrayList<>();
    }
    public ChatBotMemory(String sysMsg) {
        this.limit = null;
        this.sysMsg = sysMsg;
        List<Message> history = new ArrayList<Message>();
        history.add(new SystemMessage(sysMsg));
        this.history =  history;
    }
    public void save(Message message) {
        history.add(message);
        if(limit == null) return;
        if (history.size() > limit) {
            if(sysMsg == null){
                history.remove(0);
            } else {
                history.remove(1);
            }
        }
    }
}
