package com.donut.login.service;
// PasswordService.java (비밀번호 업데이트 전담)

import com.donut.login.mapper.LoginMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    @Autowired
    private LoginMapper loginMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public void updateUserPassword(String userId, String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        loginMapper.updateUserPassword(userId, encodedPassword);
    }
}

