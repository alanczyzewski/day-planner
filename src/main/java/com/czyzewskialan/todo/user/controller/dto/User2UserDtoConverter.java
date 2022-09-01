package com.czyzewskialan.todo.user.controller.dto;

import com.czyzewskialan.todo.user.domain.User;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class User2UserDtoConverter implements Function<User, UserDto> {

    @Override
    public UserDto apply(User user) {
        return new UserDto(user.getLogin(), user.getRole());
    }
}
