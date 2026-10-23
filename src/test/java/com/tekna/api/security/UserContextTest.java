package com.tekna.api.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserContextTest {

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("Should set, get and clear user ID in current thread")
    void testUserContext_ThreadLocal() {
        UUID userId = UUID.randomUUID();

        assertThat(UserContext.getUserId()).isNull();

        UserContext.setUserId(userId);
        assertThat(UserContext.getUserId()).isEqualTo(userId);

        UserContext.clear();
        assertThat(UserContext.getUserId()).isNull();
    }
}
