package com.example.learning.learning_habit_plan_backend.controller;
import com.example.learning.learning_habit_plan_backend.common.Result;
import com.example.learning.learning_habit_plan_backend.entity.User;
import com.example.learning.learning_habit_plan_backend.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/user")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 注册接口
    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        userService.register(user);
        return Result.success("注册成功");
    }

    // 登录接口
    @PostMapping("/login")
    public Result<User> login(@RequestBody User user) {
        User loggedUser = userService.login(user.getUsername(), user.getPassword());
        if (loggedUser == null) {
            return Result.failure("用户名或密码错误");
        }
        return Result.success(loggedUser);
    }
}