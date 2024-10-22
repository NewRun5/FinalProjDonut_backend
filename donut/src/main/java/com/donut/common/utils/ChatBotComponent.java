package com.donut.common.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatBotComponent {
    private final OpenAiChatModel chatModel;
    private final OpenAiImageModel openAiImageModel;

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
    public Message getChatResponseByMemory(ChatBotMemory memory) {
        List<Message> list = memory.getHistory();
        Prompt prompt = new Prompt(list);
        Message result =  chatModel.call(prompt).getResult().getOutput();
        memory.save(result);
        return result;
    }
    public Message getChatResponseByMemoryWithSysMsg(ChatBotMemory memory, String systemMessage) {
        List<Message> list = memory.getHistory();
        list.add(0, new SystemMessage(systemMessage));
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
    public <T> T getStructuredOutputByMemory(ChatBotMemory memory, String userInput, Class<T> resultClass){


        List<Message> history = memory.getHistory();
        history.add(new UserMessage(userInput));


        List<Message> list = new ArrayList<>(history);
        BeanOutputConverter<T>beanOutputConverter = new BeanOutputConverter<>(resultClass);
        if(history.get(0).getClass() == SystemMessage.class){
            list.add(new SystemMessage(beanOutputConverter.getFormat()));
        } else {
            list.add(0, new SystemMessage(beanOutputConverter.getFormat()));
        }

        int tried = 0;
        while (tried < 5) {
            try {
                Generation generation = chatModel.call(new Prompt(list)).getResult();
                history.add(generation.getOutput());
                T resultObj = beanOutputConverter.convert(generation.getOutput().getContent());

                // 원하는 클래스 타입인지 확인
                if (!resultClass.isInstance(resultObj)) {
                    throw new Exception("Parsing error: Expected " + resultClass.getName() + " but got different type");
                }

                list.add(generation.getOutput());
                return resultObj;
            } catch (Exception e) {
                System.out.println("parse exception: " + (tried + 1) + " tried");
                tried++;
            }
        }

        return null;
    }
    public <T> T getStructuredOutputByMemory(ChatBotMemory memory, Class<T> resultClass){


        List<Message> history = memory.getHistory();

        List<Message> list = new ArrayList<>(history);

        BeanOutputConverter<T>beanOutputConverter = new BeanOutputConverter<>(resultClass);
        if(history.get(0).getClass() == SystemMessage.class){
            list.add(new SystemMessage(beanOutputConverter.getFormat()));
        } else {
            list.add(0, new SystemMessage(beanOutputConverter.getFormat()));
        }

        int tried = 0;
        while (tried < 5) {
            try {
                Generation generation = chatModel.call(new Prompt(list)).getResult();
                T resultObj = beanOutputConverter.convert(generation.getOutput().getContent());

                // 원하는 클래스 타입인지 확인
                if (!resultClass.isInstance(resultObj)) {
                    throw new Exception("Parsing error: Expected " + resultClass.getName() + " but got different type");
                }

                list.add(generation.getOutput());
                return resultObj;
            } catch (Exception e) {
                System.out.println("parse exception: " + (tried + 1) + " tried");
                tried++;
            }
        }
        return null;
    }
    /**
     * 구조화된 출력을 얻기 위한 메소드입니다.-
     * @param sysInput : 시스템 메세지
     * @param userInput : 유저 메세지
     * @param resultClass : 원하는 클래스의 클래스 객체
     * @return : 반환된 클래스
     */
    public <T> T getStructuredOutput(String sysInput, String userInput, Class<T> resultClass) {
        Message sysMsg = new SystemMessage(sysInput);
        Message userMsg = new UserMessage(userInput);
        List<Message> list = new ArrayList<>();
        BeanOutputConverter<T> beanOutputConverter = new BeanOutputConverter<>(resultClass);
        list.add(sysMsg);
        list.add(new SystemMessage(beanOutputConverter.getFormat()));
        list.add(userMsg);

        int tried = 0;
        while (tried < 5) {
            try {
                Generation generation = chatModel.call(new Prompt(list)).getResult();
                T resultObj = beanOutputConverter.convert(generation.getOutput().getContent());

                // 원하는 클래스 타입인지 확인
                if (!resultClass.isInstance(resultObj)) {
                    throw new Exception("Parsing error: Expected " + resultClass.getName() + " but got different type");
                }

                list.add(generation.getOutput());
                return resultObj;
            } catch (Exception e) {
                System.out.println("parse exception: " + (tried + 1) + " tried");
                tried++;
            }
        }
        return null;
    }

    public String getImageByString(String prompt) {
        ImageResponse response = openAiImageModel.call(
                new ImagePrompt(prompt,
                        OpenAiImageOptions.builder()
                                .withQuality("hd")
                                .withN(1)
                                .withHeight(1792)
                                .withWidth(1024).build())

        );
        return response.getResult().getOutput().getUrl();
    }
}
