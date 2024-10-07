package com.donut.login;

import com.donut.login.login.LoginDTO;
import com.donut.login.mapper.LoginMapper;
import com.donut.login.service.LoginService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

//@SpringBootTest
//public class LoginServiceTest {
//
//    @Autowired
//    private LoginService loginService;
//
//    @Autowired
//    private LoginMapper loginMapper;
//
//    // 비밀번호 업데이트 후 암호화 확인 테스트
//    @Test
//    public void testPasswordUpdate() {
//        String newPassword = "new_password";
//        // 비밀번호 업데이트
//        loginService.updateUserPassword("test09", "new_password");
//
//        // 데이터베이스에서 사용자 정보 가져오기
//        LoginDTO user = loginMapper.findUserByUserId("test09");
//
//        // 비밀번호가 암호화되어 있는지 확인
//        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//        String encodedPassword = user.getPassword();  // DB에 저장된 암호화된 비밀번호
//        assertTrue(passwordEncoder.matches("new_password", user.getPassword()));  // 평문 비밀번호와 암호화된 비밀번호 일치 확인
//        System.out.println("암호화된 비밀번호: " + encodedPassword);
//    }
//
//    // 평문 비밀번호와 암호화된 비밀번호 비교 테스트
//    @Test
//    public void testPasswordMatch() {
//        String plainPassword = "test_password";
//        // 테스트를 위해 평문 비밀번호를 직접 설정
//        localPasswordUtil.setPlainPasswordForTesting("test09", passwordEncoder.encode(plainPassword));
//
//        // 로그인 서비스를 통해 인증 (내부적으로 암호화된 비밀번호와 비교)
//        assertTrue(loginService.authenticate("test09", plainPassword));
//    }
////        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
////
////        // 평문 비밀번호
////        String rawPassword = "new_password";  // 'test09' 계정의 업데이트된 비밀번호
////
////        // 데이터베이스에서 암호화된 비밀번호 가져오기
////        LoginDTO user = loginMapper.findUserByUserId("test09");  // 'test09' 계정에 대한 암호화된 비밀번호를 가져옴
////        String encodedPassword = user.getPassword();  // DB에 저장된 암호화된 비밀번호
////
////        // 평문 비밀번호와 암호화된 비밀번호가 일치하는지 확인
////        boolean isMatched = passwordEncoder.matches(rawPassword, encodedPassword);
////        System.out.println("비밀번호 일치 여부: " + isMatched);
////        // 결과가 참이면 테스트 성공, 거짓이면 실패
////        assertTrue(isMatched);
////    }
//
//}

import com.donut.login.service.LoginService;
import com.donut.login.mapper.LoginMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class LoginServiceTest {

    @Autowired
    private LoginService loginService;

    @Autowired
    private LoginMapper loginMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;  // PasswordEncoder 빈을 주입받습니다.


    @Autowired
    private UserDetailsService userDetailsService;  // CustomUserDetailsService 주입
    @Test
    public void testPasswordEncryptionAndLogin() {
        // 1. 사용자 ID와 평문 비밀번호 설정
        String userId = "testUser";
        String rawPassword = "testPassword123";

        // 2. 비밀번호를 암호화하고 DB에 저장
        String encodedPassword = passwordEncoder.encode(rawPassword);
        loginMapper.updateUserPassword(userId, encodedPassword); // MyBatis를 사용해 DB에 직접 저장

        // 3. 암호화된 비밀번호로 로그인 테스트
        boolean isAuthenticated = loginService.authenticate(userId, rawPassword);
        System.out.println("로그인 성공 여부: " + isAuthenticated);

        // 4. 로그인 성공 여부를 확인
        assertTrue(isAuthenticated, "로그인이 성공해야 합니다.");
    }
}
