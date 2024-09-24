package com.donut.user;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
