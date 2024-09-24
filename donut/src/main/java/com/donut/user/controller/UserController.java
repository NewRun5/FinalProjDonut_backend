package com.donut.user.controller;

import com.donut.user.dto.UserDTO;
import com.donut.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @QueryMapping
    public UserDTO getUserByUserId(@Argument String userId) {
        UserDTO resultUser = userService.findUserByUserId(userId);
        return resultUser;
    }
}
