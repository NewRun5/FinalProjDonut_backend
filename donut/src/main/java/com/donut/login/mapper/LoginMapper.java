package com.donut.login.mapper;

import com.donut.login.login.LoginDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


@Mapper
public interface LoginMapper {
    @Select("SELECT * FROM USER WHERE ID = #{userId}")
    LoginDTO findUserByUserId(@Param("userId") String userId);

    // 비밀번호 업데이트 쿼리 추가
    @Update("UPDATE USER SET PASSWORD = #{password} WHERE ID = #{userId}")
    void updateUserPassword(@Param("userId") String userId, @Param("password") String password);
}




