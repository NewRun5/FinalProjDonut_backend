package com.donut.common.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatBotComponent {
    private final OpenAiChatModel chatModel;
    public Message getChatResponse(ChatBotMemory memory, String userMessage) {
        Message msgUser = new UserMessage(userMessage);
        memory.save(msgUser);

        List<Message> list = memory.getHistory();
        list.add(msgUser);

        Prompt prompt = new Prompt(list);

        Message result =  chatModel.call(prompt).getResult().getOutput();
        memory.save(result);
        return result;
    }
}
