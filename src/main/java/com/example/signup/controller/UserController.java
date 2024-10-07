package com.example.signup.controller;

import com.example.signup.model.User;
import com.example.signup.service.UserService;
import com.example.signup.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/signup")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    private Map<String, String> verificationCodes = new HashMap<>();

    // 회원가입 API
    @PostMapping
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody User user) {
        String result = userService.registerUser(user);
        Map<String, String> response = new HashMap<>();

        if ("Username already exists!".equals(result)) {
            response.put("message", "이미 존재하는 사용자 이름입니다!");
            return ResponseEntity.status(400).body(response);
        }

        if ("Nickname already exists!".equals(result)) {
            response.put("message", "이미 존재하는 닉네임입니다!");
            return ResponseEntity.status(400).body(response);
        }

        if ("Email already exists!".equals(result)) {
            response.put("message", "이미 존재하는 이메일입니다!");
            return ResponseEntity.status(400).body(response);
        }

        response.put("message", "회원가입이 성공적으로 완료되었습니다. 이메일을 확인하세요.");
        return ResponseEntity.ok(response);
    }

    // 사용자 이름 중복 확인 API
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        boolean isAvailable = !userService.existsByUsername(username);
        return ResponseEntity.ok(Map.of("available", isAvailable));
    }

    // 닉네임 중복 확인 API
    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Boolean>> checkNickname(@RequestParam String nickname) {
        boolean isAvailable = !userService.existsByNickname(nickname);
        return ResponseEntity.ok(Map.of("available", isAvailable));
    }

    // 이메일 인증 코드 발송 API
    @PostMapping("/send-verification-code")
    public ResponseEntity<Map<String, String>> sendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String verificationCode = generateVerificationCode();

        boolean isEmailSent = emailService.sendVerificationEmail(email, verificationCode);
        Map<String, String> response = new HashMap<>();

        if (isEmailSent) {
            response.put("message", "인증 코드가 이메일로 발송되었습니다.");
            verificationCodes.put(email, verificationCode);
        } else {
            response.put("message", "이메일 발송에 실패했습니다.");
            return ResponseEntity.status(500).body(response);
        }

        return ResponseEntity.ok(response);
    }

    // 6자리 인증 코드 생성 메서드
    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000)); // 6자리 랜덤 인증 코드 생성
    }

    // 인증 코드 검증 API
    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, String>> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        String storedCode = verificationCodes.get(email);
        Map<String, String> response = new HashMap<>();

        if (storedCode != null && storedCode.equals(code)) {
            response.put("message", "인증이 성공적으로 완료되었습니다.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "잘못된 인증 코드입니다.");
            return ResponseEntity.status(400).body(response);
        }
    }
}
