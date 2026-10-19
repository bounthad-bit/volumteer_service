package com.volunteer.modules.activity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ActivityVO {
    private Long id;
    private String title;
    private String description;
    private String coverUrl;
    private String location;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
    private Integer maxNum;
    private Integer status;
    private String statusLabel;
    private Long organizerId;
    private String organizerName;
    private String auditRemark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /** 已通过审核的报名人数 */
    private Long approvedCount;
    /** 当前登录用户在该活动中的报名状态（未报名为 null） */
    private Integer myRegistrationStatus;
    /** 当前登录用户本次的签到情况：null=未签到，0=已签到未签退，1=已签退 */
    private Integer myCheckInState;
    /** 平均评分，无评价时为 null */
    private Double avgScore;
}
