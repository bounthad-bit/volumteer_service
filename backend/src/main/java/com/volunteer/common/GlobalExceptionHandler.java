package com.volunteer.common;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBiz(BizException ex) {
        return ApiResponse.fail(ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleDenied(AccessDeniedException ex) {
        return ApiResponse.fail("当前账号无权执行该操作");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .orElse("参数校验失败");
        return ApiResponse.fail(msg);
    }

    @ExceptionHandler(org.springframework.dao.DuplicateKeyException.class)
    public ApiResponse<Void> handleDuplicate(org.springframework.dao.DuplicateKeyException ex) {
        String m = ex.getMessage() == null ? "" : ex.getMessage();
        if (m.contains("uk_username")) return ApiResponse.fail("用户名已被注册");
        if (m.contains("uk_phone")) return ApiResponse.fail("手机号已被注册");
        if (m.contains("uk_reg")) return ApiResponse.fail("请勿重复报名该活动");
        if (m.contains("uk_evaluation")) return ApiResponse.fail("每个活动每人只能评价一次");
        return ApiResponse.fail("数据重复，请检查填写内容");
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ApiResponse<Void> handleUnreadable(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        return ApiResponse.fail("提交参数格式错误，请检查日期或数字格式");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleAny(Exception ex) {
        ex.printStackTrace();
        return ApiResponse.fail("系统繁忙或内部错误，请稍后重试");
    }
}
