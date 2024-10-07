package com.example.signup.service;

import com.example.signup.model.User;
import com.example.signup.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    private Map<String, String> verificationCodes = new HashMap<>();

    public String registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return "Username already exists!";
        }
        if (userRepository.existsByNickname(user.getNickname())) {
            return "Nickname already exists!";
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            return "Email already exists!";
        }

        user.setSignupDate(LocalDate.now());
        userRepository.save(user);

        String verificationCode = generateVerificationCode();
        boolean emailSent = emailService.sendVerificationEmail(user.getEmail(), verificationCode);

        if (emailSent) {
            verificationCodes.put(user.getEmail(), verificationCode);
            return "Email verification sent";
        } else {
            return "User registered successfully, but failed to send email verification.";
        }
    }

    public String verifyCode(String email, String code) {
        String storedCode = verificationCodes.get(email);

        if (storedCode != null && storedCode.equals(code)) {
            verificationCodes.remove(email);
            return "Verification code is valid.";
        } else {
            return "Invalid verification code.";
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
