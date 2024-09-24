package com.donut.user.mapper;

import com.donut.user.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    UserDTO findUserByUserId(String userId);
}
