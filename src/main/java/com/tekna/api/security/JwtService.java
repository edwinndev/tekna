package com.tekna.api.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.tekna.api.common.exception.InternalServerException;
import com.tekna.api.entity.User;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class JwtService {

    public static final long ACCESS_TOKEN_EXPIRATION_SECONDS = 3600L;
    public static final long REFRESH_TOKEN_EXPIRATION_SECONDS = 604800L;

    private static final String PRIVATE_KEY_PATH = "security/private_key.pem";
    private static final String PUBLIC_KEY_PATH = "security/public_key.pem";

    private JWSSigner signer;
    private JWSVerifier verifier;

    @PostConstruct
    public void init() {
        try {
            RSAPrivateKey privateKey = loadPrivateKey(PRIVATE_KEY_PATH);
            RSAPublicKey publicKey = loadPublicKey(PUBLIC_KEY_PATH);
            this.signer = new RSASSASigner(privateKey);
            this.verifier = new RSASSAVerifier(publicKey);
        } catch (Exception ex) {
            log.error("Failed to initialize RSA keypair from PEM files: {}", ex.getMessage());
            throw new InternalServerException("Failed to initialize security RSA keys");
        }
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(ACCESS_TOKEN_EXPIRATION_SECONDS);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(expiry))
                .build();

        SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);

        try {
            signedJwt.sign(signer);
            return signedJwt.serialize();
        } catch (JOSEException ex) {
            log.error("Error signing access token for user {}: {}", user.getUsername(), ex.getMessage());
            throw new InternalServerException("Error generating token");
        }
    }

    public boolean validateToken(String token) {
        try {
            SignedJWT signedJwt = SignedJWT.parse(token);
            if (!signedJwt.verify(verifier)) {
                log.warn("Invalid JWT signature");
                return false;
            }

            Date expirationTime = signedJwt.getJWTClaimsSet().getExpirationTime();
            if (expirationTime == null || expirationTime.before(new Date())) {
                log.warn("JWT token has expired");
                return false;
            }

            return true;
        } catch (ParseException | JOSEException ex) {
            log.warn("Failed to parse or verify JWT: {}", ex.getMessage());
            return false;
        }
    }

    public UUID getUserIdFromToken(String token) {
        try {
            SignedJWT signedJwt = SignedJWT.parse(token);
            String subject = signedJwt.getJWTClaimsSet().getSubject();
            return UUID.fromString(subject);
        } catch (ParseException ex) {
            log.error("Error extracting subject from JWT: {}", ex.getMessage());
            throw new InternalServerException("Invalid token format");
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            SignedJWT signedJwt = SignedJWT.parse(token);
            return signedJwt.getJWTClaimsSet().getStringClaim("username");
        } catch (ParseException ex) {
            log.error("Error extracting username claim from JWT: {}", ex.getMessage());
            throw new InternalServerException("Invalid token format");
        }
    }

    public String getRoleFromToken(String token) {
        try {
            SignedJWT signedJwt = SignedJWT.parse(token);
            return signedJwt.getJWTClaimsSet().getStringClaim("role");
        } catch (ParseException ex) {
            log.error("Error extracting role claim from JWT: {}", ex.getMessage());
            throw new InternalServerException("Invalid token format");
        }
    }

    public long getExpirationSeconds() {
        return ACCESS_TOKEN_EXPIRATION_SECONDS;
    }

    public long getRefreshTokenExpirationSeconds() {
        return REFRESH_TOKEN_EXPIRATION_SECONDS;
    }

    private RSAPrivateKey loadPrivateKey(String resourcePath) throws Exception {
        ClassPathResource resource = new ClassPathResource(resourcePath);
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
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        }
    }

    private RSAPublicKey loadPublicKey(String resourcePath) throws Exception {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        try (InputStream inputStream = resource.getInputStream()) {
            String keyContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(keyContent);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        }
    }
}
