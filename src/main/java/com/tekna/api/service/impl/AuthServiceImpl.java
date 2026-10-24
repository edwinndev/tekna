package com.tekna.api.service.impl;

import com.tekna.api.common.exception.BadRequestException;
import com.tekna.api.common.exception.NotFoundException;
import com.tekna.api.common.exception.UnauthorizedException;
import com.tekna.api.dto.AuthOutput;
import com.tekna.api.dto.LoginInput;
import com.tekna.api.dto.RefreshTokenInput;
import com.tekna.api.dto.RegisterInput;
import com.tekna.api.dto.UserOutput;
import com.tekna.api.entity.RefreshToken;
import com.tekna.api.entity.Role;
import com.tekna.api.entity.User;
import com.tekna.api.mapper.UserMapper;
import com.tekna.api.repository.RefreshTokenRepository;
import com.tekna.api.repository.UserRepository;
import com.tekna.api.security.JwtService;
import com.tekna.api.security.UserContext;
import com.tekna.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AuthOutput login(LoginInput input) {
        log.info("Attempting login for user: {}", input.getUsername());

        User user = userRepository.findByUsername(input.getUsername().trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!passwordEncoder.matches(input.getPassword(), user.getPassword())) {
            log.warn("Invalid password for user: {}", input.getUsername());
            throw new UnauthorizedException("Invalid username or password");
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            log.warn("Inactive user tried to log in: {}", user.getUsername());
            throw new UnauthorizedException("User account is disabled");
        }

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = createRefreshToken(user);

        return AuthOutput.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationSeconds())
                .user(userMapper.toOutput(user))
                .build();
    }

    @Override
    @Transactional
    public AuthOutput register(RegisterInput input) {
        log.info("Registering new user with username: {}", input.getUsername());

        String username = input.getUsername().trim().toLowerCase();
        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Username is already taken");
        }

        UUID newUserId = UUID.randomUUID();
        User user = userMapper.toEntity(input);
        user.setId(newUserId);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        user.setRole(Role.USER);
        user.setIsActive(true);

        UUID currentUserId = UserContext.getUserId();
        user.setCreatedBy(currentUserId != null ? currentUserId : newUserId);

        User savedUser = userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(savedUser);
        RefreshToken refreshToken = createRefreshToken(savedUser);

        return AuthOutput.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationSeconds())
                .user(userMapper.toOutput(savedUser))
                .build();
    }

    @Override
    @Transactional
    public AuthOutput refreshToken(RefreshTokenInput input) {
        log.info("Refreshing access token");

        RefreshToken currentRefreshToken = refreshTokenRepository.findByToken(input.getRefreshToken().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (Boolean.TRUE.equals(currentRefreshToken.getRevoked())) {
            log.warn("Attempt to use revoked refresh token for user: {}", currentRefreshToken.getUser().getUsername());
            throw new UnauthorizedException("Refresh token has been revoked");
        }

        if (currentRefreshToken.getExpiresAt().isBefore(Instant.now())) {
            log.warn("Attempt to use expired refresh token for user: {}", currentRefreshToken.getUser().getUsername());
            throw new UnauthorizedException("Refresh token has expired");
        }

        User user = currentRefreshToken.getUser();
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new UnauthorizedException("User account is disabled");
        }

        currentRefreshToken.setRevoked(true);
        currentRefreshToken.setUpdatedBy(user.getId());
        refreshTokenRepository.save(currentRefreshToken);

        String newAccessToken = jwtService.generateAccessToken(user);
        RefreshToken newRefreshToken = createRefreshToken(user);

        return AuthOutput.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationSeconds())
                .user(userMapper.toOutput(user))
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        log.info("Logging out and revoking refresh token");
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenRepository.findByToken(refreshToken.trim()).ifPresent(token -> {
                token.setRevoked(true);
                token.setUpdatedBy(token.getUser().getId());
                refreshTokenRepository.save(token);
            });
        }
    }

    @Override
    public UserOutput getMe(UUID userId) {
        return userRepository.findById(userId)
                .map(userMapper::toOutput)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", ""));
        refreshToken.setExpiresAt(Instant.now().plusSeconds(jwtService.getRefreshTokenExpirationSeconds()));
        refreshToken.setRevoked(false);
        refreshToken.setCreatedBy(user.getId());
        return refreshTokenRepository.save(refreshToken);
    }
}
