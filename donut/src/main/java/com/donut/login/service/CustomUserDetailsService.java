package com.donut.login.service;

import com.donut.login.login.LoginDTO;
import com.donut.login.mapper.LoginMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final LoginMapper loginMapper;
     // Spring 컨텍스트에서 주입

    public CustomUserDetailsService(LoginMapper loginMapper) {
        this.loginMapper = loginMapper;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LoginDTO user = loginMapper.findUserByUserId(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return User.builder()
                .username(user.getId())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }
}
