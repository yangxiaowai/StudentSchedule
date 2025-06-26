package com.example.learning.learning_habit_plan_backend.mapper;

import com.example.learning.learning_habit_plan_backend.entity.User;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface UserMapper {
    
    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByUsername(String username);
    
    @Select("SELECT * FROM user WHERE email = #{email}")
    User selectByEmail(String email);
    
    @Select("SELECT * FROM user WHERE id = #{id}")
    User selectById(Long id);
    
    @Insert("INSERT INTO user(username, password, email, is_active, created_at, updated_at) " +
            "VALUES(#{username}, #{password}, #{email}, #{isActive}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
    
    @Update("UPDATE user SET username = #{username}, password = #{password}, " +
            "email = #{email}, is_active = #{isActive}, updated_at = NOW() WHERE id = #{id}")
    int updateById(User user);
    
    @Update("UPDATE user SET password = #{password}, updated_at = NOW() WHERE id = #{id}")
    int updatePasswordById(@Param("id") Long id, @Param("password") String password);
}
