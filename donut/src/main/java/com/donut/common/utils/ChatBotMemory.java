package com.donut.common.utils;

import com.donut.curriculum.langGraph.model.ChatHistory;
import lombok.Getter;
import lombok.ToString;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatBotMemory {
    private final Integer limit;
    private final String sysMsg;
    @Getter
    private final List<Message> history;

    /**
     * 대화 내역 기억에 제한을 둘 때 사용합니다.
     * @param limit : 제한할 길이
     * @param sysMsg : 처음 정해둔 시스템 메세지
     */
    public ChatBotMemory(int limit, String sysMsg) {
        this.limit = limit * 2 + 1;
        this.sysMsg = sysMsg;
        List<Message> history = new ArrayList<Message>();
        history.add(new SystemMessage(sysMsg));
        this.history =  history;
    }

    /**
     * 시스템 메세지 없이 생성할 때 사용합니다.
     * @param limit : 제한할 길이
     */
    public ChatBotMemory(int limit) {
        this.limit = limit * 2;
        this.sysMsg = null;
        this.history = new ArrayList<>();
    }

    /**
     * 제한 없이 기억할 때 사용합니다.
     * @param sysMsg : 고정해둘 시스템 메세지입니다.
     */
    public ChatBotMemory(String sysMsg) {
        this.limit = null;
        this.sysMsg = sysMsg;
        List<Message> history = new ArrayList<Message>();
        history.add(new SystemMessage(sysMsg));
        this.history =  history;
    }

    /**
     * 대화 내역을 따로 기억시킬 때 사용합니다.
     * @param message : 기억시킬 메세지 (시스템 or 유저)
     */
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

    public void convert(List<ChatHistory> chatHistoryList) {
        for (ChatHistory history : chatHistoryList){
            if(history.getSender().equals("system")){
                this.save(new SystemMessage(history.getMessage()));
            }
            if(history.getSender().equals("user")){
                this.save(new UserMessage(history.getMessage()));
            }
        }
    }
}
