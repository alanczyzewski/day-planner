package com.czyzewskialan.todo.user.controller.dto;

import com.czyzewskialan.todo.user.domain.User;

public record UserDto(String login, User.Role role) {
}
