package com.donut.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Properties;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/graphql")  // GraphQL 엔드포인트에 대해 CORS 설정
                .allowedOriginPatterns("*")  // 프론트엔드 URL을 허용
                .allowedMethods("POST")  // 허용할 HTTP 메서드
                .allowedHeaders("*")  // 모든 헤더 허용
                .allowCredentials(true);  // 자격 증명 허용 (예: 쿠키)
    }
    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com"); // SMTP 서버 주소
        mailSender.setPort(587); // SMTP 포트

        mailSender.setUsername("skm5860@gmail.com"); // 자신의 이메일 계정
        mailSender.setPassword("mtkrbrjqlunwqfzf"); // 자신의 이메일 비밀번호

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp"); // 전송 프로토콜
        props.put("mail.smtp.auth", "true"); // SMTP 인증을 활성화
        props.put("mail.smtp.starttls.enable", "true"); // TLS 사용
        props.put("mail.debug", "true"); // 디버그 모드 활성화

        return mailSender;
    }
}
