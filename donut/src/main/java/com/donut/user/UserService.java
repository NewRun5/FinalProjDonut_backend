package com.donut.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public UserDTO findUserByUserId(String userId) {
        UserDTO result = userMapper.findUserByUserId(userId);
        return result;
    }

    public boolean registerUser(UserDTO userDTO) {
        userDTO.setRole("USER");
        userDTO.setSignupDate(LocalDate.now());
        userDTO.setPassword(bCryptPasswordEncoder.encode(userDTO.getPassword()));
        int result = userMapper.registerUser(userDTO);
        if(result == 0) return false;
        return true;
    }
}
