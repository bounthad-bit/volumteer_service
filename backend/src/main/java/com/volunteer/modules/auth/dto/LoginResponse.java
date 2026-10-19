package com.volunteer.modules.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String realName;
    private String phone;
    private String studentNo;
    private Integer role;
    private String roleLabel;
}
