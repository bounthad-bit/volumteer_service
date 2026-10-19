package com.volunteer.modules.registration.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RegistrationVO {
    private Long id;
    private Long activityId;
    private String activityTitle;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime activityStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime activityEndTime;
    private String activityLocation;
    private Integer activityStatus;
    private String activityStatusLabel;

    private Long userId;
    private String realName;
    private String username;
    private String phone;
    private String studentNo;

    private Integer status;
    private String statusLabel;
    private String reviewRemark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime registerTime;

    /** 签到状态：null=未签到 0=已签到未签退 1=已签退 */
    private Integer checkInState;
    /** 本次服务时长（分钟） */
    private Integer duration;
}
