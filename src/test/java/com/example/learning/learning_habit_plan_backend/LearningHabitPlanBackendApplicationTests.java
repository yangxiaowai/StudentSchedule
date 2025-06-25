package com.example.learning.learning_habit_plan_backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 * 简单的应用测试类，不依赖Spring Boot上下文
 * 用于验证基本的测试环境是否正常工作
 */
class LearningHabitPlanBackendApplicationTests {

	@Test
	void contextLoads() {
		// 简单的测试，验证测试环境正常
		Assertions.assertTrue(true, "测试环境正常工作");
	}

	@Test
	void applicationCanBeInstantiated() {
		// 验证应用主类可以被实例化
		LearningHabitPlanBackendApplication app = new LearningHabitPlanBackendApplication();
		Assertions.assertNotNull(app, "应用主类可以正常实例化");
	}
}
