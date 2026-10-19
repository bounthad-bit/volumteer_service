package com.volunteer.modules.auth;

import com.volunteer.modules.auth.dto.LoginRequest;
import com.volunteer.modules.auth.dto.LoginResponse;
import com.volunteer.modules.auth.dto.RegisterRequest;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    LoginResponse register(RegisterRequest request);
}
