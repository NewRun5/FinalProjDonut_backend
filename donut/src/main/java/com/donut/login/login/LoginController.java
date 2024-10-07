package com.donut.login.login;

import com.donut.login.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class LoginController {
    private final LoginService loginService;

    @MutationMapping
    public Boolean login(@Argument String userId, @Argument String password) {
        return loginService.authenticate(userId, password);
    }

    @MutationMapping
    public boolean logout() {
        SecurityContextHolder.clearContext();  // 세션과 인증 정보 제거
        return true;  // 로그아웃 성공 시 true 반환
    }
}
