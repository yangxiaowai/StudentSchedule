package com.example.learning.learning_habit_plan_backend.common.handler;


import com.example.learning.learning_habit_plan_backend.common.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Result<?>> handleMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException e) {
        Result<?> result = Result.failure("媒体类型不支持: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .header("Content-Type", "application/json")
                .body(result);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleAllException(Exception e) {
        Result<?> result = Result.failure(e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body(result);
    }

    // 可选：你可以添加更多异常类型处理，比如
    // @ExceptionHandler(RuntimeException.class)
    // @ExceptionHandler(MethodArgumentNotValidException.class)
}

