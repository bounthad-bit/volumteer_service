package com.volunteer.modules.activity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityRequest {
    @NotBlank(message = "活动名称不能为空")
    @Size(max = 50, message = "活动名称不能超过 50 个字符")
    private String title;

    @Size(max = 2000, message = "活动说明过长")
    private String description;

    /** 活动封面图路径，可选 */
    @Size(max = 200, message = "封面图路径过长")
    private String coverUrl;

    @NotBlank(message = "活动地点不能为空")
    @Size(max = 100, message = "活动地点过长")
    private String location;

    @NotNull(message = "请选择活动开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    @NotNull(message = "请选择活动结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    @NotNull(message = "请填写招募人数")
    @Min(value = 1, message = "招募人数至少为 1")
    private Integer maxNum;
}
