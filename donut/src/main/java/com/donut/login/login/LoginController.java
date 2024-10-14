package com.donut.login.login;

import com.donut.login.service.LoginService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class LoginController {
    private final LoginService loginService;
    private final HttpSession session; // HttpSession 주입
    @MutationMapping
    public String login(@Argument String userId, @Argument String password) {
        boolean authenticated = loginService.authenticate(userId, password);
        if (authenticated) {
            // 인증된 사용자 정보로 Authentication 객체 생성
            Authentication authentication = new UsernamePasswordAuthenticationToken(userId, password);

            // SecurityContextHolder에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 세션에 사용자 정보 저장 (예: userId를 세션에 저장)
            session.setAttribute("user", userId);  // 세션에 사용자 ID 저장

            // 세션 ID 반환 또는 사용자 정보 반환
            return session.getId();
        }
        return null; // 로그인 실패 시 null 반환
    }


    @MutationMapping
    public boolean logout() {
        loginService.logout(session);
        return true;
    }
    @MutationMapping
    public boolean checkSession() {
        String userId = (String) session.getAttribute("user");
        if(userId != null){
            System.out.println(userId.equals("null"));
            return !userId.equals("null");
        }
        return false;
    }
}
