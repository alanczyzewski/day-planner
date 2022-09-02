package com.czyzewskialan.todo.utils;

import com.czyzewskialan.todo.user.domain.User;

public class LoggingUtils {
    static final String OBFUSCATED_PART = "*****";

    private LoggingUtils() {
    }

    public static User obfuscatePasswordHash(User user) {
        return user.toBuilder().passwordHash(OBFUSCATED_PART).build();
    }
}
