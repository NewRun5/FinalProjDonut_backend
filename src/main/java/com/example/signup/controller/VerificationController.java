package com.example.signup.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.signup.service.EmailService;

@RestController
@RequestMapping("/api")
public class VerificationController {

    private static final Logger logger = LoggerFactory.getLogger(VerificationController.class);

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private EmailService emailService;

    // 이메일과 인증 코드를 저장하는 Map
    private Map<String, String> verificationCodes = new HashMap<>();

    @PostMapping("/send-verification-code")
    public ResponseEntity<String> sendVerificationCode(@RequestBody EmailRequest emailRequest) {
        String email = emailRequest.getEmail();
        String verificationCode = generateRandomCode(6);

        // 이메일 발송 서비스 호출
        boolean emailSent = emailService.sendVerificationEmail(email, verificationCode);
        if (!emailSent) {
            return ResponseEntity.status(500).body("이메일 발송에 실패했습니다.");
        }

        // 인증 코드를 로그에 출력
        logger.info("Generated verification code for {}: {}", email, verificationCode);

        // Map에 인증 코드 저장
        verificationCodes.put(email, verificationCode);
        return ResponseEntity.ok("인증 코드가 이메일로 발송되었습니다.");
    }

    // 인증 코드 검증
    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestBody VerificationRequest verificationRequest) {
        String email = verificationRequest.getEmail();
        String code = verificationRequest.getCode();

        // 저장된 인증 코드와 입력한 코드가 일치하는지 확인
        String storedCode = verificationCodes.get(email);

        // 디버그: 저장된 코드와 입력된 코드 로그 출력
        logger.info("Stored code for {}: {}", email, storedCode);
        logger.info("Entered code: {}", code);

        if (storedCode != null && storedCode.equals(code)) {
            return ResponseEntity.ok("인증 완료");
        } else {
            logger.error("Invalid verification code for {}: {}", email, code); // 인증 실패 로그
            return ResponseEntity.status(400).body("잘못된 인증 코드입니다.");
        }
    }

    private String generateRandomCode(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(length);
        String characters = "0123456789";

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            code.append(characters.charAt(index));
        }
        return code.toString();
    }
}

class EmailRequest {
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

class VerificationRequest {
    private String email;
    private String code;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
