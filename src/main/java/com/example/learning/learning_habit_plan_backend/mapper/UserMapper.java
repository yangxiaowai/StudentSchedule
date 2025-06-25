package com.example.learning.learning_habit_plan_backend.mapper;

import com.example.learning.learning_habit_plan_backend.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface UserMapper {
    @Select("SELECT * FROM users WHERE username = #{username}")
    User selectByUsername(String username);
    
    @Insert("INSERT INTO users(username, password, email) VALUES(#{username}, #{password}, #{email})")
    int insert(User user);
}
