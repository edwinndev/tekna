package com.tekna.api.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.tekna.api.entity.Role;
import com.tekna.api.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        jwtService.init();

        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("nimbususer@tekna.com");
        user.setRole(Role.ADMIN);
        user.setIsActive(true);
    }

    @Test
    @DisplayName("Should generate valid JWT signed with RSA RS256 using PEM keys")
    void generateAccessToken_Success() {
        String token = jwtService.generateAccessToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.getUsernameFromToken(token)).isEqualTo("nimbususer@tekna.com");
        assertThat(jwtService.getUserIdFromToken(token)).isEqualTo(user.getId());
        assertThat(jwtService.getRoleFromToken(token)).isEqualTo(Role.ADMIN.name());
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(3600L);
        assertThat(jwtService.getRefreshTokenExpirationSeconds()).isEqualTo(604800L);
    }

    @Test
    @DisplayName("Should return false for invalid or tampered JWT")
    void validateToken_TamperedToken_ReturnsFalse() {
        String token = jwtService.generateAccessToken(user);
        String tamperedToken = token + "tampered";

        assertThat(jwtService.validateToken(tamperedToken)).isFalse();
    }

    @Test
    @DisplayName("Should return false for expired token")
    void validateToken_ExpiredToken_ReturnsFalse() throws Exception {
        ClassPathResource resource = new ClassPathResource("security/private_key.pem");
        RSAPrivateKey privateKey;
        try (InputStream inputStream = resource.getInputStream()) {
            String keyContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(keyContent);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        }

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .issueTime(Date.from(Instant.now().minusSeconds(100)))
                .expirationTime(Date.from(Instant.now().minusSeconds(10)))
                .build();

        SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);
        signedJwt.sign(new RSASSASigner(privateKey));
        String expiredToken = signedJwt.serialize();

        assertThat(jwtService.validateToken(expiredToken)).isFalse();
    }
}
