package com.donut.user;

import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @QueryMapping
    public UserDTO getUserByUserId(@Argument String userId, DataFetchingEnvironment env) {
        String query = env.getOperationDefinition().toString();
        System.out.println("GraphQL Query: " + query);
        return userService.findUserByUserId(userId);
    }
}
