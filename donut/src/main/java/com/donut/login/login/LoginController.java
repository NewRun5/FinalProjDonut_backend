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
        return loginService.logout();
    }
}
