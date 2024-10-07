package com.donut.user;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    UserDTO findUserByUserId(String userId);
}
