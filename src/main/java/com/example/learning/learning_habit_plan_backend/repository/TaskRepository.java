package com.example.learning.learning_habit_plan_backend.repository;
import com.example.learning.learning_habit_plan_backend.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository

public interface TaskRepository extends JpaRepository<Task, Long> {
}