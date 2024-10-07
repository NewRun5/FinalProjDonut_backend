package com.donut.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration("webConfigCommon")
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 모든 엔드포인트에 대해 CORS 허용
                .allowedOrigins("http://localhost:3001", "http://localhost:3000") // 여러 도메인 허용
                .allowedMethods("POST", "GET", "PUT", "DELETE", "OPTIONS") // 다양한 HTTP 메서드 허용
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With") // 필요한 헤더 허용
                .allowCredentials(true);  // 쿠키 및 인증 정보 허용
    }
}