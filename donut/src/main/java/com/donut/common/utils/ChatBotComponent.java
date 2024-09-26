package com.donut.common.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
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

    /**
     * memory 객체를 통해 응답을 받는 메소드입니다.
     * @param memory : 기존의 메세지를 기억하기 위한 객체
     * @param userMessage : 응답을 받을 질문
     * @return : 결과 Message 객체
     */
    public Message getChatResponseByMemory(ChatBotMemory memory, String userMessage) {
        Message msgUser = new UserMessage(userMessage);
        memory.save(msgUser);

        List<Message> list = memory.getHistory();
        list.add(msgUser);

        Prompt prompt = new Prompt(list);

        Message result =  chatModel.call(prompt).getResult().getOutput();
        memory.save(result);
        return result;
    }

    /**
     * 시스템 메세지를 가진 1회성 응답을 받기 위한 메소드입니다.
     * @param sysInput : 시스템 메세지
     * @param userInput : 유저 메세지
     * @return : 결과 Message 객체
     */
    public Message getChatResponseWithSysMsg(String sysInput, String userInput){
        Message sysMsg = new SystemMessage(sysInput);
        Message userMsg = new UserMessage(userInput);
        List<Message> list = new ArrayList<>();
        list.add(sysMsg);
        list.add(userMsg);

        Prompt prompt = new Prompt(list);

        return chatModel.call(prompt).getResult().getOutput();
    }

    /**
     * 시스템 메세지 없이 1회성 응답을 받기 위한 메소드입니다.
     * @param userInput : 유저 메세지
     * @return : 결과 Message 객체
     */
    public Message getChatResponse(String userInput){
        Message userMsg = new UserMessage(userInput);
        Prompt prompt = new Prompt(userMsg);
        return chatModel.call(prompt).getResult().getOutput();
    }

    /**
     * 메모리를 사용하여 구조화된 출력을 얻기 위한 메소드입니다.
     * @param memory : 메모리 객체
     * @param userInput : 유저 쿼리
     * @param resultClass : 결과 클래스 객체
     * @return : 반환된 클래스
     */
    public Object getStructuredOutputByMemory(ChatBotMemory memory, String userInput, Class<?> resultClass){


        List<Message> history = memory.getHistory();
        history.add(new UserMessage(userInput));


        List<Message> list = new ArrayList<>(history);
        BeanOutputConverter<?>beanOutputConverter = new BeanOutputConverter<>(resultClass);
        if(history.get(0).getClass() == SystemMessage.class){
            list.add(new SystemMessage(beanOutputConverter.getFormat()));
        } else {
            list.add(0, new SystemMessage(beanOutputConverter.getFormat()));
        }


        Generation generation = chatModel.call(new Prompt(list)).getResult();
        Object resultObj = beanOutputConverter.convert(generation.getOutput().getContent());

        history.add(generation.getOutput());
        return resultObj;
    }
    /**
     * 구조화된 출력을 얻기 위한 메소드입니다.
     * 아직 테스트 되지 않았습니다.
     * @param sysInput : 시스템 메세지
     * @param userInput : 유저 메세지
     * @param resultClass : 원하는 클래스의 클래스 객체
     * @return : 반환된 클래스
     */
    public Object getStructuredOutput(String sysInput, String userInput, Class<?> resultClass){
        Message sysMsg = new SystemMessage(sysInput);
        Message userMsg = new UserMessage(userInput);
        List<Message> list = new ArrayList<>();
        BeanOutputConverter<?>beanOutputConverter = new BeanOutputConverter<>(resultClass);
        list.add(sysMsg);
        list.add(new SystemMessage(beanOutputConverter.getFormat()));
        list.add(userMsg);

        Generation generation = chatModel.call(new Prompt(list)).getResult();
        Object resultObj = beanOutputConverter.convert(generation.getOutput().getContent());
        list.add(generation.getOutput());
        return resultObj;
    }
}
