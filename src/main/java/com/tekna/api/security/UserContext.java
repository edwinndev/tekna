package com.tekna.api.security;

import java.util.UUID;

public final class UserContext {

    private static final ThreadLocal<UUID> USER_ID = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setUserId(UUID userId) {
        USER_ID.set(userId);
    }

    public static UUID getUserId() {
        return USER_ID.get();
    }

    public static void clear() {
        USER_ID.remove();
    }
}
