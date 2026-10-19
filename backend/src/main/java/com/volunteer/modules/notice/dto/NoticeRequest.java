package com.volunteer.modules.notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NoticeRequest {
    @NotBlank(message = "公告标题不能为空")
    @Size(max = 50, message = "标题不能超过 50 个字符")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    private String content;
}
