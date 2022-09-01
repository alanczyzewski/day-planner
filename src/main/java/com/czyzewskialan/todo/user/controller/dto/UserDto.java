package com.czyzewskialan.todo.user.controller.dto;

import com.czyzewskialan.todo.user.domain.User;

import java.time.LocalDateTime;

public record UserDto(String login, User.Role role, LocalDateTime dateCreated, LocalDateTime dateUpdated) {
}
