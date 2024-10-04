package com.donut.login;

import com.donut.login.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class LoginService {
    private final LoginMapper loginMapper;

    // 사용자 등록 (비밀번호 암호화 없이 단순 등록)
//    public void registerUser(String userId, String password) {
//        loginMapper.insertUser(userId, password);  // 비밀번호를 해시하지 않고 저장
//    }

    // 비밀번호 확인 메서드 추가 (GraphQL 로그인용)
    public boolean authenticate(String userId, String password) {
        System.out.println("로그인 시도: userId = " + userId + ", password = " + password);  // 로그 추가
        LoginDTO user = loginMapper.findUserByUserId(userId);
        if (user == null) {
            System.out.println("사용자 없음: " + userId);
            throw new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND.value());
        }
        if (!user.getPassword().equals(password)) {
            System.out.println("비밀번호 불일치: " + user.getPassword() + " != " + password);
            throw new CustomException("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED.value());
        }
        System.out.println("로그인 성공");
        return true;
    }
}
