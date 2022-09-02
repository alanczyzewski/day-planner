package com.czyzewskialan.todo.utils;

import com.czyzewskialan.todo.user.domain.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.czyzewskialan.todo.utils.LoggingUtils.OBFUSCATED_PART;
import static org.assertj.core.api.Assertions.assertThat;

class LoggingUtilsTest {

    @Test
    void shouldObfuscatePasswordHash() {
        //given
        User user = User.builder().login("login").passwordHash("PasswordHash").role(User.Role.ADMIN)
                .dateCreated(LocalDateTime.now()).dateUpdated(LocalDateTime.now()).build();

        //when
        User obfuscatedUser = LoggingUtils.obfuscatePasswordHash(user);

        //then
        assertThat(obfuscatedUser).isNotEqualTo(user);
        assertThat(obfuscatedUser.getPasswordHash()).isNotEqualTo(user.getPasswordHash()).isEqualTo(OBFUSCATED_PART);
        assertThat(obfuscatedUser.getLogin()).isEqualTo(user.getLogin());
        assertThat(obfuscatedUser.getRole()).isEqualTo(user.getRole());
        assertThat(obfuscatedUser.getDateCreated()).isEqualTo(user.getDateCreated());
        assertThat(obfuscatedUser.getDateUpdated()).isEqualTo(user.getDateUpdated());
    }
}