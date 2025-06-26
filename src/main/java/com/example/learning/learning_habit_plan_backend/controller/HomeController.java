package com.example.learning.learning_habit_plan_backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "forward:/index1.html";
    }

    @GetMapping("/home")
    public String homePage() {
        return "forward:/index.html";
    }
}