package com.volunteer.common;

/** 业务异常，统一被 GlobalExceptionHandler 转换为 code=500 的响应。 */
public class BizException extends RuntimeException {
    public BizException(String message) {
        super(message);
    }
}
