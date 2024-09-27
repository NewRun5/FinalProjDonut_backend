package com.donut.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    public UserDTO findUserByUserId(String userId) {
        UserDTO result = userMapper.findUserByUserId(userId);
        return result;
    }
}