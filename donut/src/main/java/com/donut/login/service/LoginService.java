package com.donut.login.service;

import com.donut.login.exception.CustomException;
import com.donut.login.login.LoginDTO;
import com.donut.login.mapper.LoginMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final PasswordEncoder passwordEncoder;
//    private final BCryptPasswordEncoder passwordEncoder;
    private final LoginMapper loginMapper;

    // 로그인 인증 처리
    public boolean authenticate(String userId, String password) {
        // 데이터베이스에서 사용자 정보 조회
        LoginDTO user = loginMapper.findUserByUserId(userId);
        System.out.println(user.getPassword());
        if (user == null) {
            throw new CustomException("User not found", HttpStatus.NOT_FOUND.value());
        }

        // 디버깅을 위한 로그 출력 (비밀번호 비교 전)
        System.out.println("입력된 비밀번호: " + password);
        System.out.println("DB에 저장된 암호화된 비밀번호: " + user.getPassword());

        // 입력된 비밀번호와 DB의 암호화된 비밀번호 비교
        boolean isMatch = passwordEncoder.matches(password, user.getPassword());
        System.out.println("비밀번호 일치 여부: " + isMatch);

        if (!isMatch) {
            throw new CustomException("Invalid credentials", HttpStatus.UNAUTHORIZED.value());
        }

        return isMatch;  // 비밀번호가 일치하면 true 반환
    }

    // 회원가입 또는 비밀번호 암호화 후 저장
    public void registerUser(String userId, String rawPassword) {
        // 비밀번호를 BCrypt로 암호화해서 저장
        String encodedPassword = passwordEncoder.encode(rawPassword);  // 비밀번호 암호화
        loginMapper.updateUserPassword(userId, encodedPassword);
    }

    public boolean logout(HttpSession session) {
        // 세션에 사용자 정보가 존재하는지 확인
        if (session == null || session.isNew()) {
            System.out.println("세션이 유효하지 않습니다.");
            return false;
        }

        // 현재 인증된 사용자의 SecurityContext 가져오기
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        if (authentication != null) {
            String userId = authentication.getName();  // 현재 인증된 사용자 ID 가져오기
            System.out.println("로그아웃 요청된 사용자 ID: " + userId);
        } else {
            System.out.println("로그아웃 시 인증된 사용자가 없습니다.");
        }

        // SecurityContext 클리어
        SecurityContextHolder.clearContext();

        // 세션 무효화
        session.invalidate();

        return true;
    }

}
