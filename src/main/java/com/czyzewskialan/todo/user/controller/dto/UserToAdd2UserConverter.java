package com.czyzewskialan.todo.user.controller.dto;

import com.czyzewskialan.todo.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class UserToAdd2UserConverter implements Function<UserToAddDto, User> {
    private final PasswordEncoder passwordEncoder;

    @Override
    public User apply(UserToAddDto user) {
        return User.builder()
                .login(user.getLogin())
                .passwordHash(passwordEncoder.encode(user.getPassword()))
                .role(user.getRole()).build();
    }
}
