package com.example.learning.learning_habit_plan_backend.service;

import com.example.learning.learning_habit_plan_backend.dto.AIResourceResponse;

import java.util.List;

public interface AISearchService {
    List<AIResourceResponse> searchLearningResources(String query);
}
