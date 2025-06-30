package com.example.learning.learning_habit_plan_backend.controller;

import com.example.learning.learning_habit_plan_backend.entity.Task;
import com.example.learning.learning_habit_plan_backend.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    @Autowired
    private TaskService taskService;

    @PostMapping("/tasks")
    public Map<String, Object> analyzeTasks(@RequestBody List<Long> taskIds) {
        // 1. 查询所有任务
        List<Task> tasks = taskService.getTasksByIds(taskIds);

        // 2. 聚合数据
        List<String> subjectNames = Arrays.asList("语文", "数学", "英语", "物理", "化学");
        List<String> dayNames = Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日");

        // 统计每个学科的平均完成度
        Map<String, List<Integer>> subjectProgressMap = new HashMap<>();
        for (String subject : subjectNames) subjectProgressMap.put(subject, new ArrayList<>());
        for (Task t : tasks) {
            if (subjectProgressMap.containsKey(t.getSubject()) && t.getProgress() != null) {
                subjectProgressMap.get(t.getSubject()).add(t.getProgress());
            }
        }
        List<Integer> subjectData = subjectNames.stream()
            .map(s -> {
                List<Integer> progresses = subjectProgressMap.get(s);
                return progresses.isEmpty() ? 0 : (int) progresses.stream().mapToInt(i -> i).average().orElse(0);
            })
            .toList();

        // 统计每天的平均完成度
        Map<String, List<Integer>> dayProgressMap = new HashMap<>();
        for (String day : dayNames) dayProgressMap.put(day, new ArrayList<>());
        for (Task t : tasks) {
            if (t.getStartTime() != null && t.getProgress() != null) {
                int dayOfWeek = t.getStartTime().getDayOfWeek().getValue(); // 1=Monday
                String dayName = dayNames.get(dayOfWeek - 1);
                dayProgressMap.get(dayName).add(t.getProgress());
            }
        }
        List<Integer> dayData = dayNames.stream()
            .map(d -> {
                List<Integer> progresses = dayProgressMap.get(d);
                return progresses.isEmpty() ? 0 : (int) progresses.stream().mapToInt(i -> i).average().orElse(0);
            })
            .toList();

        // 计划数据（可根据实际业务调整，这里用固定值）
        List<Integer> planSubjects = Arrays.asList(80, 80, 85, 75, 70);
        List<Integer> planDays = Arrays.asList(85, 80, 85, 80, 75, 70, 90);

        // 3. 组织prompt
        String prompt = String.format(
            "学生本周各学科完成度为：%s，原计划为：%s。每日完成度为：%s，原计划为：%s。请结合这些数据，给出具体、简明的学习建议，内容不超过80字。",
            subjectData, planSubjects, dayData, planDays
        );

        // 4. 调用AI
        String suggestion = callDeepSeekAPI(prompt);

        // 5. 返回
        Map<String, Object> result = new HashMap<>();
        result.put("subjectData", subjectData);
        result.put("dayData", dayData);
        result.put("planSubjects", planSubjects);
        result.put("planDays", planDays);
        result.put("subjectNames", subjectNames);
        result.put("dayNames", dayNames);
        result.put("suggestion", suggestion);
        return result;
    }

    private String callDeepSeekAPI(String prompt) {
        String apiUrl = "https://api.deepseek.com/v1/chat/completions";
        String apiKey = "sk-5aa250a96eb74458aa923dc5ee49f4ce";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "deepseek-chat");
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", prompt));
        payload.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
            Map body = response.getBody();
            if (body != null && body.containsKey("choices")) {
                List choices = (List) body.get("choices");
                if (!choices.isEmpty()) {
                    Map choice = (Map) choices.get(0);
                    Map message = (Map) choice.get("message");
                    return message.get("content").toString().trim();
                }
            }
        } catch (Exception e) {
            return "AI建议生成失败";
        }
        return "AI建议生成失败";
    }
}
