package com.example.learning.learning_habit_plan_backend.service;

import com.example.learning.learning_habit_plan_backend.entity.User;

public interface UserService {
    User login(String username, String password);
    void register(User user);
}

