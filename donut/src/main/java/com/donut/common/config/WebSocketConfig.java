package com.donut.common.config;

import com.donut.chapter.questionChatBot.QuestionChatBotHandler;
import com.donut.curriculum.chatBot.CurriculumChatbotWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final CurriculumChatbotWebSocketHandler curriculumHandler;
    private final QuestionChatBotHandler questionHandler;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(curriculumHandler, "/ws-chat").setAllowedOrigins("*");
//        registry.addHandler(questionHandler, "/ws-chat").setAllowedOrigins("*");
    }
}
