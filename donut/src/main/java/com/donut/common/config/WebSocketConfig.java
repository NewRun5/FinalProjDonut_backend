package com.donut.common.config;

import com.donut.chapter.questionChatBot.QuestionChatBotHandler;
import com.donut.curriculum.chatBot.CurriculumChatbotWebSocketHandler;
import com.donut.refrenceOnly.contentChatBot.langGraph.TestWSHandler;
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
    private final TestWSHandler testWSHandler;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(curriculumHandler, "/ws-generate-curriculum").setAllowedOrigins("*");
        registry.addHandler(testWSHandler, "/ws-chat").setAllowedOrigins("*");
    }
}
