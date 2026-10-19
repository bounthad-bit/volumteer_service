package com.volunteer.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginUser {
    private Long userId;
    private String username;
    private String realName;
    private Integer role;
}
