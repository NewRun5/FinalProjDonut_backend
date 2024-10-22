package com.donut.user;

import com.donut.user.service.VerificationService;
import graphql.schema.DataFetchingEnvironment;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final VerificationService verificationService;
    private final HttpSession session; // HttpSession 주입

    /* 이메일 저장용 세션 */
    private Map<String, String> verificationSession = new HashMap<>();
    @QueryMapping
    public UserDTO getUserBySession() {
        String userId = (String) session.getAttribute("user");
        return userService.findUserByUserId(userId);
    }
    @MutationMapping
    public boolean registerUser(@Argument("input") UserDTO userDTO) {
        boolean result = userService.registerUser(userDTO);
        return result;
    }
    @MutationMapping
    public boolean sendVerificationCode(@Argument String email){
        String verificationCode = verificationService.generateRandomCode(6);
        boolean emailSent = verificationService.sendVerificationEmail(email, verificationCode);
        if (!emailSent) {
            return false;
        }
        verificationSession.put(email, verificationCode);
        return true;
    }
    @MutationMapping
    public boolean sendVerificationEmail(@Argument String email, @Argument String code){
        if (!verificationSession.containsKey(email)) return false;
        if (!verificationSession.get(email).equals(code)) return false;
        return true;
    }
}
