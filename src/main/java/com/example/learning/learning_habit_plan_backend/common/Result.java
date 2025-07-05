package com.example.learning.learning_habit_plan_backend.common;

//import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
//@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {
    private static final int SUCCESS_CODE = 200;
    private int code;
    private String message;
    private T data;

    // 明确指定三参数构造函数（虽然@AllArgsConstructor已经提供了，但为了确保清晰）
    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, "操作成功", data);
    }

    public static <T> Result<T> success() {
        return new Result<>(200, "成功", null);
    }

    public static <T> Result<T> failure(String message) {
        return new Result<>(500, message, null);
    }

    public static <T> Result<T> failure(int code, String message) {
        return new Result<>(code, message, null);
    }
}
