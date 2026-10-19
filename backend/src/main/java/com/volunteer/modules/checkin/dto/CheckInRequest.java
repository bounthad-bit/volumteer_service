package com.volunteer.modules.checkin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CheckInRequest {
    /** 报名记录 ID，签到与签退共用 */
    @jakarta.validation.constraints.NotNull(message = "缺少报名记录")
    private Long registrationId;

    /** 报名时登记的手机号，用于二次确认，防止代签 */
    @NotBlank(message = "请输入报名时登记的手机号")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
