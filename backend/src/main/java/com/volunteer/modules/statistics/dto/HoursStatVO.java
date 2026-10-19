package com.volunteer.modules.statistics.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HoursStatVO {
    private Long userId;
    private String username;
    private String realName;
    private String phone;
    private Long activityCount;
    private Long totalMinutes;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastServiceTime;
}
