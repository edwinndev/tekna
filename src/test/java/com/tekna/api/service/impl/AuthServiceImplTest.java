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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private UUID userId;
    private User user;
    private UserOutput userOutput;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setUsername("gamertest");
        user.setEmail("gamertest@tekna.com");
        user.setPassword("encoded_password");
        user.setRole(Role.USER);
        user.setIsActive(true);

        userOutput = new UserOutput();
        userOutput.setId(userId);
        userOutput.setUsername("gamertest");
        userOutput.setEmail("gamertest@tekna.com");
        userOutput.setRole(Role.USER);
        userOutput.setIsActive(true);
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void login_Success() {
        LoginInput input = new LoginInput();
        input.setUsernameOrEmail("gamertest");
        input.setPassword("password123");

        when(userRepository.findByUsernameOrEmail("gamertest")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("jwt.token.here");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);
        when(jwtService.getRefreshTokenExpirationSeconds()).thenReturn(604800L);
        when(userMapper.toOutput(user)).thenReturn(userOutput);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthOutput result = authService.login(input);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("jwt.token.here");
        assertThat(result.getRefreshToken()).isNotBlank();
        assertThat(result.getTokenType()).isEqualTo("Bearer");
        assertThat(result.getExpiresIn()).isEqualTo(3600L);
        assertThat(result.getUser()).isEqualTo(userOutput);
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when login credentials password does not match")
    void login_InvalidPassword_ThrowsUnauthorizedException() {
        LoginInput input = new LoginInput();
        input.setUsernameOrEmail("gamertest");
        input.setPassword("wrongpassword");

        when(userRepository.findByUsernameOrEmail("gamertest")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encoded_password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(input))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid username/email or password");

        verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when user does not exist on login")
    void login_UserNotFound_ThrowsUnauthorizedException() {
        LoginInput input = new LoginInput();
        input.setUsernameOrEmail("nonexistent");
        input.setPassword("password123");

        when(userRepository.findByUsernameOrEmail("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(input))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid username/email or password");
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when user account is inactive on login")
    void login_InactiveUser_ThrowsUnauthorizedException() {
        user.setIsActive(false);
        LoginInput input = new LoginInput();
        input.setUsernameOrEmail("gamertest");
        input.setPassword("password123");

        when(userRepository.findByUsernameOrEmail("gamertest")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(input))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("User account is disabled");
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void register_Success() {
        RegisterInput input = new RegisterInput();
        input.setUsername("newgamer");
        input.setEmail("newgamer@tekna.com");
        input.setPassword("Secret123*");

        when(userRepository.existsByUsername("newgamer")).thenReturn(false);
        when(userRepository.existsByEmail("newgamer@tekna.com")).thenReturn(false);
        when(userMapper.toEntity(input)).thenReturn(new User());
        when(passwordEncoder.encode("Secret123*")).thenReturn("encoded_secret");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn("jwt.token.new");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);
        when(jwtService.getRefreshTokenExpirationSeconds()).thenReturn(604800L);
        when(userMapper.toOutput(user)).thenReturn(userOutput);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthOutput result = authService.register(input);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("jwt.token.new");
        assertThat(result.getUser()).isEqualTo(userOutput);
    }

    @Test
    @DisplayName("Should throw BadRequestException when registering with duplicate username")
    void register_DuplicateUsername_ThrowsBadRequestException() {
        RegisterInput input = new RegisterInput();
        input.setUsername("gamertest");
        input.setEmail("different@tekna.com");
        input.setPassword("Secret123*");

        when(userRepository.existsByUsername("gamertest")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(input))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Username is already taken");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BadRequestException when registering with duplicate email")
    void register_DuplicateEmail_ThrowsBadRequestException() {
        RegisterInput input = new RegisterInput();
        input.setUsername("uniquename");
        input.setEmail("gamertest@tekna.com");
        input.setPassword("Secret123*");

        when(userRepository.existsByUsername("uniquename")).thenReturn(false);
        when(userRepository.existsByEmail("gamertest@tekna.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(input))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email is already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should refresh token successfully with valid active refresh token")
    void refreshToken_Success() {
        RefreshToken token = new RefreshToken();
        token.setToken("valid_refresh_token");
        token.setUser(user);
        token.setRevoked(false);
        token.setExpiresAt(Instant.now().plusSeconds(3600));

        RefreshTokenInput input = new RefreshTokenInput();
        input.setRefreshToken("valid_refresh_token");

        when(refreshTokenRepository.findByToken("valid_refresh_token")).thenReturn(Optional.of(token));
        when(jwtService.generateAccessToken(user)).thenReturn("new.access.token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);
        when(jwtService.getRefreshTokenExpirationSeconds()).thenReturn(604800L);
        when(userMapper.toOutput(user)).thenReturn(userOutput);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthOutput result = authService.refreshToken(input);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("new.access.token");
        assertThat(token.getRevoked()).isTrue();
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when refresh token is revoked")
    void refreshToken_RevokedToken_ThrowsUnauthorizedException() {
        RefreshToken token = new RefreshToken();
        token.setToken("revoked_token");
        token.setUser(user);
        token.setRevoked(true);
        token.setExpiresAt(Instant.now().plusSeconds(3600));

        RefreshTokenInput input = new RefreshTokenInput();
        input.setRefreshToken("revoked_token");

        when(refreshTokenRepository.findByToken("revoked_token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.refreshToken(input))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Refresh token has been revoked");
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when refresh token is expired")
    void refreshToken_ExpiredToken_ThrowsUnauthorizedException() {
        RefreshToken token = new RefreshToken();
        token.setToken("expired_token");
        token.setUser(user);
        token.setRevoked(false);
        token.setExpiresAt(Instant.now().minusSeconds(10));

        RefreshTokenInput input = new RefreshTokenInput();
        input.setRefreshToken("expired_token");

        when(refreshTokenRepository.findByToken("expired_token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.refreshToken(input))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Refresh token has expired");
    }

    @Test
    @DisplayName("Should get current user profile successfully")
    void getMe_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toOutput(user)).thenReturn(userOutput);

        UserOutput result = authService.getMe(userId);

        assertThat(result).isEqualTo(userOutput);
    }

    @Test
    @DisplayName("Should throw NotFoundException when current user is not found")
    void getMe_NotFound_ThrowsNotFoundException() {
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getMe(nonExistentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found with id: " + nonExistentId);
    }
}
