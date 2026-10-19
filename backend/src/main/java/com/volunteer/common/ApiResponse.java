package com.volunteer.common;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 统一响应体，前端按 code/msg/data 三部分统一处理。
 */
@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private Integer code;
    private String msg;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(200, "success", null);
    }

    public static <T> ApiResponse<T> fail(String msg) {
        return new ApiResponse<>(500, msg, null);
    }
}
