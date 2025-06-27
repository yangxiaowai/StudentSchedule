package com.example.learning.learning_habit_plan_backend.service.impl;

import com.example.learning.learning_habit_plan_backend.dto.AIResourceResponse;
import com.example.learning.learning_habit_plan_backend.service.AISearchService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;


@Service
public class AISearchServiceImpl implements AISearchService {

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AISearchServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<AIResourceResponse> searchLearningResources(String query) {
        try {
            String prompt = buildPrompt(query);
            String requestBody = buildRequestBody(prompt);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            return parseApiResponse(response.getBody());
        } catch (Exception e) {
            System.err.println("AI搜索服务出错: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private String buildPrompt(String query) {
        return String.format(
                "你是一个学习资源推荐专家，请根据用户的学习主题推荐2个最相关的优质学习网站。要求：1. 必须是知名、权威的学习平台 2. 包含完整的网站名称和可访问的URL 3. 对每个网站提供50字左右的简要说明，突出其特点和优势 4. 返回格式必须为严格的JSON数组，每个对象包含title、url和description字段。用户的学习主题是：%s",
                query
        ).replace("\n", " "); // 移除换行符
    }


    private String buildRequestBody(String prompt) throws JsonProcessingException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-chat");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "user",
                "content", prompt
        ));
        requestBody.put("messages", messages);

        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 500);

        return objectMapper.writeValueAsString(requestBody);
    }


    private List<AIResourceResponse> parseApiResponse(String responseBody) throws Exception {
        List<AIResourceResponse> results = new ArrayList<>();

        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode choices = rootNode.path("choices");

        if (choices.isArray() && choices.size() > 0) {
            String content = choices.get(0).path("message").path("content").asText();

            // 去除Markdown代码块标记
            content = content.replace("```json", "").replace("```", "").trim();

            try {
                JsonNode resources = objectMapper.readTree(content);
                if (resources.isArray()) {
                    for (JsonNode resource : resources) {
                        AIResourceResponse item = new AIResourceResponse();
                        item.setTitle(resource.path("title").asText());
                        item.setUrl(resource.path("url").asText());
                        item.setDescription(resource.path("description").asText());
                        results.add(item);
                    }
                }
            } catch (Exception e) {
                // 如果解析失败，返回默认结果
                return createFallbackResults(content);
            }
        }

        return results;
    }


    private List<AIResourceResponse> createFallbackResults(String content) {
        List<AIResourceResponse> results = new ArrayList<>();

        AIResourceResponse item1 = new AIResourceResponse();
        item1.setTitle("学习资源1");
        item1.setUrl("https://example.com/resource1");
        item1.setDescription(content);
        results.add(item1);

        AIResourceResponse item2 = new AIResourceResponse();
        item2.setTitle("学习资源2");
        item2.setUrl("https://example.com/resource2");
        item2.setDescription(content);
        results.add(item2);

        return results;
    }
}
