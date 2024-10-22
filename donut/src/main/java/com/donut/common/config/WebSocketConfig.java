package com.donut.common.config;

import com.donut.chapter.questionChatBot.QuestionChatBotHandler;
import com.donut.curriculum.chatBot.CurriculumChatbotWebSocketHandler;
import com.donut.refrenceOnly.contentChatBot.langGraph.TestWSHandler;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.websocket.server.UriTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final CurriculumChatbotWebSocketHandler curriculumHandler;
    private final QuestionChatBotHandler questionHandler;
    private final TestWSHandler testWSHandler;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(testWSHandler, "/ws-chat")
                .setAllowedOrigins("*")
                .addInterceptors(new HttpSessionHandshakeInterceptor());  // HTTP 세션 공유
        registry.addHandler(curriculumHandler, "/ws-generate-curriculum")
                .setAllowedOrigins("*")
                .addInterceptors(new HttpSessionHandshakeInterceptor());  // HTTP 세션 공유
        registry.addHandler(questionHandler, "/ws-chapter-chat/{chapterId}")
                .setAllowedOrigins("*")
                .addInterceptors(new HttpSessionHandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(
                            ServerHttpRequest request,
                            ServerHttpResponse response,
                            WebSocketHandler wsHandler,
                            Map<String, Object> attributes) throws Exception {
                        super.beforeHandshake(request, response, wsHandler, attributes);

                        // 경로에서 직접 chapterId 추출
                        String path = request.getURI().getPath();
                        String[] pathParts = path.split("/");
                        if (pathParts.length > 2) {
                            String chapterId = pathParts[pathParts.length - 1];
                            if (chapterId == null || chapterId.isEmpty()) {
                                chapterId = "none";  // 기본값 설정 또는 그냥 빈 값으로 연결 허용
                            }
                            attributes.put("chapterId", chapterId);
                        }
                        return true;  // 항상 true로 반환하여 연결 허용
                    }
                });
    }
}
