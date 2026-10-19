package com.volunteer.modules.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String studentNo;
    private Integer role;
    private String roleLabel;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    /** 累计服务时长（分钟），仅个人相关接口填充 */
    private Long totalDuration;
    /** 累计参加活动数 */
    private Long activityCount;
}
