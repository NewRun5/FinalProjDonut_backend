package com.donut.login;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


@Mapper
public interface LoginMapper {
    @Select("SELECT * FROM USER WHERE ID = #{userId}")
    LoginDTO findUserByUserId(@Param("userId") String userId);
}
