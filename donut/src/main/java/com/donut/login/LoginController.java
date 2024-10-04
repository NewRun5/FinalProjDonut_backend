package com.donut.login;

import com.donut.login.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@RequiredArgsConstructor
@SessionAttributes("userId")
public class LoginController {
    private final LoginService loginService;

    // 로그인 (세션 기반)
    @MutationMapping
    public Boolean login(@Argument String userId, @Argument String password) {
        if (loginService.authenticate(userId, password)) {
            // 세션 처리는 서비스 레이어에서 하거나, JWT 토큰 방식으로 처리
            return true;
        } else {
            throw new CustomException("아이디나 비밀번호가 잘못되었습니다.", HttpStatus.UNAUTHORIZED.value());
        }
    }

    // 로그아웃
    @MutationMapping
    public boolean logout() {
        return true;
    }
}
