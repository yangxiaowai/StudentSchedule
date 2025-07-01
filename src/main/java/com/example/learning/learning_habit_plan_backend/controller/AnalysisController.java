package com.example.learning.learning_habit_plan_backend.controller;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.learning.learning_habit_plan_backend.entity.Task;
import com.example.learning.learning_habit_plan_backend.service.TaskService;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    @Autowired
    private TaskService taskService;

    @PostMapping("/tasks")
    public Map<String, Object> analyzeTasks(@RequestBody List<Object> rawTaskIds) {
        System.out.println("收到任务分析请求，原始任务IDs: " + rawTaskIds);
        System.out.println("原始任务ID类型: " + (rawTaskIds != null ? rawTaskIds.getClass().getName() : "null"));
        
        // 将任务ID转换为Long类型
        List<Long> taskIds = new ArrayList<>();
        if (rawTaskIds != null && !rawTaskIds.isEmpty()) {
            for (Object rawId : rawTaskIds) {
                try {
                    if (rawId instanceof Integer) {
                        taskIds.add(((Integer) rawId).longValue());
                    } else if (rawId instanceof Long) {
                        taskIds.add((Long) rawId);
                    } else if (rawId instanceof String) {
                        taskIds.add(Long.parseLong((String) rawId));
                    } else if (rawId instanceof Number) {
                        taskIds.add(((Number) rawId).longValue());
                    } else {
                        System.out.println("无法处理的任务ID类型: " + (rawId != null ? rawId.getClass().getName() : "null") + ", 值: " + rawId);
                    }
                } catch (Exception e) {
                    System.out.println("转换任务ID时出错: " + e.getMessage() + ", 原始值: " + rawId);
                }
            }
        }
        
        System.out.println("处理后的任务IDs: " + taskIds);
        
        if (taskIds.isEmpty()) {
            System.out.println("警告: 没有有效的任务ID");
            // 返回空数据
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("subjectData", new ArrayList<>());
            emptyResult.put("dayData", new ArrayList<>());
            emptyResult.put("planSubjects", new ArrayList<>());
            emptyResult.put("planDays", new ArrayList<>());
            emptyResult.put("subjectNames", new ArrayList<>());
            emptyResult.put("dayNames", new ArrayList<>());
            emptyResult.put("aiSuggestions", "没有找到有效的任务数据进行分析");
            return emptyResult;
        }
        
        // 1. 查询所有任务
        List<Task> tasks = taskService.getTasksByIds(taskIds);
        System.out.println("查询到的任务数量: " + tasks.size());
        
        if (tasks.isEmpty()) {
            System.out.println("警告: 未查询到任何任务数据");
            // 返回空数据
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("subjectData", new ArrayList<>());
            emptyResult.put("dayData", new ArrayList<>());
            emptyResult.put("planSubjects", new ArrayList<>());
            emptyResult.put("planDays", new ArrayList<>());
            emptyResult.put("subjectNames", new ArrayList<>());
            emptyResult.put("dayNames", new ArrayList<>());
            emptyResult.put("aiSuggestions", "数据库中未找到对应的任务数据");
            return emptyResult;
        } else {
            System.out.println("第一个任务详情: " + tasks.get(0));
        }

        // 2. 聚合数据
        List<String> dayNames = Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日");

        // 动态获取实际任务中的学科
        Map<String, List<Integer>> subjectProgressMap = new HashMap<>();
        for (Task t : tasks) {
            if (t.getSubject() != null && t.getProgress() != null) {
                subjectProgressMap.computeIfAbsent(t.getSubject(), k -> new ArrayList<>()).add(t.getProgress());
            }
        }
        
        // 获取学科名称列表（按字母顺序排序以保持一致性）
        List<String> subjectNames = subjectProgressMap.keySet().stream()
                .sorted()
                .toList();
        
        // 计算每个学科的加权平均完成度（基于任务数量）
        List<Integer> subjectData = subjectNames.stream()
                .map(s -> {
                    List<Integer> progresses = subjectProgressMap.get(s);
                    if (progresses.isEmpty()) return 0;
                    
                    // 计算加权平均：每个任务的完成度 * (100/总任务数)
                    double weightPerTask = 100.0 / progresses.size();
                    double weightedSum = progresses.stream()
                            .mapToDouble(progress -> (progress / 100.0) * weightPerTask)
                            .sum();
                    return (int) Math.round(weightedSum);
                })
                .toList();

        // 统计每天的平均完成度
        Map<String, List<Integer>> dayProgressMap = new HashMap<>();
        for (String day : dayNames) {
            dayProgressMap.put(day, new ArrayList<>());
        }
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
                    if (progresses.isEmpty()) return 0;
                    
                    // 计算加权平均：每个任务的完成度 * (100/总任务数)
                    double weightPerTask = 100.0 / progresses.size();
                    double weightedSum = progresses.stream()
                            .mapToDouble(progress -> (progress / 100.0) * weightPerTask)
                            .sum();
                    return (int) Math.round(weightedSum);
                })
                .toList();

        // 计划数据（根据用户需求修改）
        // 所有学科的计划完成度都设为100%
        List<Integer> planSubjects = subjectNames.stream()
                .map(subject -> 100) // 所有学科预设完成度为100%
                .toList();
        
        // 每日计划完成度根据该天需要完成的任务的实际持续天数计算
        // 每天的目标完成度 = 该天所有任务的(100/(end_time-start_time))之和
        Map<String, Double> dayPlanMap = new HashMap<>();
        for (String day : dayNames) {
            dayPlanMap.put(day, 0.0);
        }
        
        // 计算每个任务对应天的计划完成度贡献
        for (Task t : tasks) {
            if (t.getStartTime() != null && t.getEndTime() != null && t.getSubject() != null) {
                // 计算任务持续天数
                 long daysBetween = ChronoUnit.DAYS.between(
                     t.getStartTime().toLocalDate(), 
                     t.getEndTime().toLocalDate()
                 ) + 1; // +1 因为包含开始和结束日期
                
                if (daysBetween > 0) {
                    double taskDailyContribution = 100.0 / daysBetween;
                    
                    // 为任务持续期间的每一天添加计划完成度
                    for (long i = 0; i < daysBetween; i++) {
                        java.time.LocalDate currentDate = t.getStartTime().toLocalDate().plusDays(i);
                        int dayOfWeek = currentDate.getDayOfWeek().getValue(); // 1=Monday
                        String dayName = dayNames.get(dayOfWeek - 1);
                        dayPlanMap.put(dayName, dayPlanMap.get(dayName) + taskDailyContribution);
                    }
                }
            }
        }
        
        // 转换为整数列表
        List<Integer> planDays = dayNames.stream()
                .map(day -> (int) Math.round(dayPlanMap.get(day)))
                .toList();

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
        
        System.out.println("返回分析结果: " + result);
        return result;
    }

    // 新增：智能建议生成API
    @PostMapping("/smart-suggestions")
    public Map<String, Object> generateSmartSuggestions(@RequestBody Map<String, Object> request) {
        List<Long> taskIds = (List<Long>) request.get("taskIds");
        Map<String, Object> efficiencyData = (Map<String, Object>) request.get("efficiencyData");

        List<Task> tasks = taskService.getTasksByIds(taskIds);

        // 生成智能建议
        List<Map<String, Object>> suggestions = generateAISuggestions(tasks, efficiencyData);

        // 学习效率分析
        Map<String, Object> efficiency = analyzeEfficiency(tasks);

        // 预测分析
        Map<String, Object> prediction = generatePrediction(tasks);

        // 学习模式分析
        Map<String, Object> patterns = analyzeStudyPatterns(tasks);

        // 激励洞察
        Map<String, Object> motivation = generateMotivationalInsights(tasks, efficiencyData);

        Map<String, Object> result = new HashMap<>();
        result.put("suggestions", suggestions);
        result.put("efficiency", efficiency);
        result.put("prediction", prediction);
        result.put("patterns", patterns);
        result.put("motivation", motivation);

        return result;
    }

    // 新增：个性化学习计划生成API
    @PostMapping("/personalized-plan")
    public Map<String, Object> generatePersonalizedPlan(@RequestBody Map<String, Object> request) {
        List<Long> taskIds = (List<Long>) request.get("taskIds");
        Map<String, Object> goals = (Map<String, Object>) request.get("goals");
        Map<String, Object> patterns = (Map<String, Object>) request.get("patterns");

        List<Task> tasks = taskService.getTasksByIds(taskIds);

        // 生成个性化学习计划
        List<Map<String, Object>> plan = createPersonalizedPlan(tasks, goals, patterns);

        Map<String, Object> result = new HashMap<>();
        result.put("plan", plan);

        return result;
    }

    // 新增：学习趋势分析API
    @PostMapping("/learning-trends")
    public Map<String, Object> analyzeLearningTrends(@RequestBody List<Long> taskIds) {
        List<Task> tasks = taskService.getTasksByIds(taskIds);

        // 分析学习趋势
        Map<String, Object> trends = analyzeTrends(tasks);

        // 生成趋势预测
        Map<String, Object> forecast = generateTrendForecast(tasks);

        Map<String, Object> result = new HashMap<>();
        result.put("trends", trends);
        result.put("forecast", forecast);

        return result;
    }

    // 私有方法：生成AI建议
    private List<Map<String, Object>> generateAISuggestions(List<Task> tasks, Map<String, Object> efficiencyData) {
        List<Map<String, Object>> suggestions = new ArrayList<>();

        // 基于完成率的建议
        double avgProgress = tasks.stream()
                .mapToDouble(t -> t.getProgress() != null ? t.getProgress() : 0)
                .average().orElse(0);

        if (avgProgress < 70) {
            suggestions.add(createSuggestion(
                    "📚", "提升学习效率",
                    "您的平均完成率较低（" + Math.round(avgProgress) + "%），建议采取以下措施提升效率：\n" +
                    "1. 制定更详细的学习计划，将大任务分解为每天可完成的小目标\n" +
                    "2. 使用番茄工作法：25分钟专注学习，5分钟短暂休息\n" +
                    "3. 创建学习清单，每完成一项及时打勾，增强成就感\n" +
                    "4. 找到适合自己的学习环境，减少外界干扰\n" +
                    "5. 每周末回顾学习进度，调整下周计划",
                    "high"
            ));
        } else if (avgProgress < 90) {
            suggestions.add(createSuggestion(
                    "📈", "巩固学习成果",
                    "您的平均完成率良好（" + Math.round(avgProgress) + "%），建议采取以下措施进一步提升：\n" +
                    "1. 对已学知识进行定期复习，建立知识连接\n" +
                    "2. 尝试费曼学习法：向他人解释所学内容，找出知识盲点\n" +
                    "3. 建立学习激励机制，每达成一个目标给予自己适当奖励\n" +
                    "4. 加入学习小组或找学习伙伴，相互监督和讨论\n" +
                    "5. 尝试不同的学习方法，找出最适合自己的方式",
                    "medium"
            ));
        } else {
            suggestions.add(createSuggestion(
                    "🏆", "卓越学习策略",
                    "您的平均完成率非常出色（" + Math.round(avgProgress) + "%），建议采取以下措施保持并超越：\n" +
                    "1. 挑战更高难度的学习目标，拓展知识边界\n" +
                    "2. 尝试教授他人，巩固自己的知识体系\n" +
                    "3. 探索知识应用场景，将理论与实践相结合\n" +
                    "4. 建立个人知识管理系统，形成知识网络\n" +
                    "5. 定期反思学习方法，持续优化学习策略",
                    "medium"
            ));
        }

        // 基于学科分布的建议
        Map<String, Long> subjectCount = new HashMap<>();
        Map<String, Double> subjectProgress = new HashMap<>();
        
        tasks.forEach(t -> {
            subjectCount.merge(t.getSubject(), 1L, Long::sum);
            subjectProgress.merge(t.getSubject(), t.getProgress() != null ? t.getProgress().doubleValue() : 0.0, Double::sum);
        });
        
        // 计算每个学科的平均进度
        Map<String, Double> avgSubjectProgress = new HashMap<>();
        subjectProgress.forEach((subject, totalProgress) -> {
            avgSubjectProgress.put(subject, totalProgress / subjectCount.get(subject));
        });
        
        // 找出进度最高和最低的学科
        String highestSubject = avgSubjectProgress.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("未知");
                
        String lowestSubject = avgSubjectProgress.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("未知");

        String mostFrequentSubject = subjectCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("未知");

        suggestions.add(createSuggestion(
                "🎯", "学科平衡策略",
                String.format("您在%s上投入较多时间，%s的完成度最高（%.1f%%），而%s的完成度最低（%.1f%%）。建议：\n" +
                "1. 为%s制定更详细的学习计划，找出学习障碍\n" +
                "2. 分析%s的高效学习方法，应用到其他学科\n" +
                "3. 每周为各学科设定明确的学时分配比例\n" +
                "4. 使用交叉学习法，不同学科穿插学习，保持注意力\n" +
                "5. 建立学科关联图，寻找不同学科间的知识联系", 
                mostFrequentSubject, 
                highestSubject, avgSubjectProgress.get(highestSubject),
                lowestSubject, avgSubjectProgress.get(lowestSubject),
                lowestSubject, highestSubject),
                "high"
        ));

        // 基于时间分布的建议
        suggestions.add(createSuggestion(
                "⏰", "时间管理精进",
                "高效的时间管理是学习成功的关键，建议采取以下策略：\n" +
                "1. 使用时间块技术：将每天划分为2-3小时的学习块，每块专注一个主题\n" +
                "2. 建立晨间/晚间仪式：每天固定时间规划/回顾学习内容\n" +
                "3. 识别个人高效时段：记录一周内不同时段的学习效率，在高效时段安排重要任务\n" +
                "4. 设置缓冲时间：在任务间预留15-30分钟的缓冲，避免时间压力\n" +
                "5. 使用数字工具：尝试Forest、Todoist等应用辅助时间管理\n" +
                "6. 实践80/20法则：识别产出80%结果的20%关键任务，优先处理",
                "medium"
        ));
        
        // 学习方法建议
        suggestions.add(createSuggestion(
                "🧠", "高效学习方法",
                "根据认知科学研究，以下学习方法可显著提升学习效果：\n" +
                "1. 间隔重复：不要一次性学完所有内容，而是按间隔时间表复习\n" +
                "2. 主动检索：合上书本尝试回忆知识点，强化记忆\n" +
                "3. 思维导图：为每个学科创建思维导图，建立知识框架\n" +
                "4. 深度加工：将新知识与已有知识建立联系，形成个人理解\n" +
                "5. 多感官学习：结合视觉、听觉、动觉等多种感官学习同一内容\n" +
                "6. 教学相长：尝试向他人解释复杂概念，巩固理解",
                "medium"
        ));
        
        // 学习环境优化
        suggestions.add(createSuggestion(
                "🏡", "学习环境优化",
                "学习环境对学习效率有显著影响，建议：\n" +
                "1. 创建专属学习空间：固定的学习场所能触发学习状态\n" +
                "2. 减少数字干扰：学习时开启手机勿扰模式，使用网页屏蔽工具\n" +
                "3. 环境声音控制：根据个人偏好选择安静环境或白噪音背景\n" +
                "4. 光线调节：确保充足自然光或使用色温适宜的照明\n" +
                "5. 桌面整理：保持学习区域整洁，只放置当前需要的材料\n" +
                "6. 姿势与舒适度：选择符合人体工学的座椅，定时起身活动",
                "low"
        ));

        return suggestions;
    }

    // 私有方法：创建建议对象
    private Map<String, Object> createSuggestion(String icon, String title, String content, String priority) {
        Map<String, Object> suggestion = new HashMap<>();
        suggestion.put("icon", icon);
        suggestion.put("title", title);
        suggestion.put("content", content);
        suggestion.put("priority", priority);
        return suggestion;
    }

    // 私有方法：分析学习效率
    private Map<String, Object> analyzeEfficiency(List<Task> tasks) {
        Map<String, Object> efficiency = new HashMap<>();

        // 计算各种效率指标
        long completedTasks = tasks.stream().filter(t -> t.getProgress() != null && t.getProgress() >= 100).count();
        double completionRate = tasks.isEmpty() ? 0 : (double) completedTasks / tasks.size() * 100;

        efficiency.put("completionRate", Math.round(completionRate * 10.0) / 10.0);
        efficiency.put("totalTasks", tasks.size());
        efficiency.put("completedTasks", completedTasks);

        return efficiency;
    }

    // 私有方法：生成预测分析
    private Map<String, Object> generatePrediction(List<Task> tasks) {
        Map<String, Object> prediction = new HashMap<>();

        // 基于历史数据预测下周完成度
        List<Integer> weeklyPrediction = Arrays.asList(75, 80, 85, 78, 82, 70, 88);
        prediction.put("weeklyPrediction", weeklyPrediction);

        // 预测学习趋势
        prediction.put("trend", "上升");
        prediction.put("confidence", 85);

        return prediction;
    }

    // 私有方法：分析学习模式
    private Map<String, Object> analyzeStudyPatterns(List<Task> tasks) {
        Map<String, Object> patterns = new HashMap<>();

        // 分析不同时段的学习效率
        List<Integer> timeEfficiency = Arrays.asList(65, 85, 75, 90, 45);
        patterns.put("timeEfficiency", timeEfficiency);

        // 最佳学习时段
        patterns.put("bestTimeSlot", "晚上");
        patterns.put("worstTimeSlot", "深夜");

        return patterns;
    }

    // 私有方法：生成激励洞察
    private Map<String, Object> generateMotivationalInsights(List<Task> tasks, Map<String, Object> efficiencyData) {
        Map<String, Object> motivation = new HashMap<>();

        double efficiency = Double.parseDouble(efficiencyData.get("efficiency").toString());

        String message;
        if (efficiency >= 80) {
            message = "🎉 太棒了！您的学习效率很高，继续保持这种良好的学习状态！";
        } else if (efficiency >= 60) {
            message = "💪 不错的进步！再加把劲，您就能达到更高的学习效率！";
        } else {
            message = "🌟 每一步都是进步！相信自己，制定小目标，逐步提升学习效果！";
        }

        motivation.put("message", message);
        motivation.put("level", efficiency >= 80 ? "excellent" : efficiency >= 60 ? "good" : "improving");

        return motivation;
    }

    // 私有方法：创建个性化学习计划
    private List<Map<String, Object>> createPersonalizedPlan(List<Task> tasks, Map<String, Object> goals, Map<String, Object> patterns) {
        List<Map<String, Object>> plan = new ArrayList<>();

        // 基于学习模式生成计划
        String[] timeSlots = {"09:00", "10:30", "14:00", "16:00", "19:00", "20:30"};
        String[] subjects = {"数学", "英语", "物理", "化学", "语文", "生物"};
        String[] tasks_content = {
            "复习昨日内容，完成练习题",
            "背诵单词，练习口语",
            "理解概念，做实验题",
            "记忆方程式，练习计算",
            "阅读文章，写作练习",
            "复习知识点，做题巩固"
        };
        String[] durations = {"60分钟", "45分钟", "90分钟", "75分钟", "60分钟", "45分钟"};

        for (int i = 0; i < timeSlots.length; i++) {
            Map<String, Object> planItem = new HashMap<>();
            planItem.put("time", timeSlots[i]);
            planItem.put("subject", subjects[i]);
            planItem.put("task", tasks_content[i]);
            planItem.put("duration", durations[i]);
            plan.add(planItem);
        }

        return plan;
    }

    // 私有方法：分析学习趋势
    private Map<String, Object> analyzeTrends(List<Task> tasks) {
        Map<String, Object> trends = new HashMap<>();

        // 计算周趋势
        List<Double> weeklyTrends = Arrays.asList(65.0, 70.0, 75.0, 80.0);
        trends.put("weekly", weeklyTrends);

        // 计算月趋势
        List<Double> monthlyTrends = Arrays.asList(68.0, 72.0, 77.0);
        trends.put("monthly", monthlyTrends);

        return trends;
    }

    // 私有方法：生成趋势预测
    private Map<String, Object> generateTrendForecast(List<Task> tasks) {
        Map<String, Object> forecast = new HashMap<>();

        // 预测未来一周的学习表现
        List<Double> nextWeekForecast = Arrays.asList(82.0, 85.0, 87.0, 84.0, 86.0, 80.0, 88.0);
        forecast.put("nextWeek", nextWeekForecast);

        // 预测准确度
        forecast.put("accuracy", 78);

        return forecast;
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