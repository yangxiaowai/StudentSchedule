package com.example.learning.learning_habit_plan_backend.controller;

import com.example.learning.learning_habit_plan_backend.dto.AIResourceResponse;
import com.example.learning.learning_habit_plan_backend.service.AISearchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai-search")
public class AISearchController {

    private final AISearchService aiSearchService;

    public AISearchController(AISearchService aiSearchService) {
        this.aiSearchService = aiSearchService;
    }

    @GetMapping
    public List<AIResourceResponse> searchResources(@RequestParam String query) {
        return aiSearchService.searchLearningResources(query);
    }
}

