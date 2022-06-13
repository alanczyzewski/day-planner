package com.czyzewskialan.todo.user.controller.dto;

import com.czyzewskialan.todo.user.domain.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UserDto {

    private final String login;
    private final User.Role role;

    public UserDto(User user) {
        login = user.getLogin();
        role = user.getRole();
    }

}
