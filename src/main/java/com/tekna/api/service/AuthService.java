package com.tekna.api.service;

import com.tekna.api.dto.AuthOutput;
import com.tekna.api.dto.LoginInput;
import com.tekna.api.dto.RefreshTokenInput;
import com.tekna.api.dto.RegisterInput;
import com.tekna.api.dto.UserOutput;

import java.util.UUID;

public interface AuthService {

    AuthOutput login(LoginInput input);

    AuthOutput register(RegisterInput input);

    AuthOutput refreshToken(RefreshTokenInput input);

    void logout(String refreshToken);

    UserOutput getMe(UUID userId);
}
