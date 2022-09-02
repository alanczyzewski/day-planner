package com.czyzewskialan.todo.todo.controller.dto;

import com.czyzewskialan.todo.todo.domain.Todo;
import com.czyzewskialan.todo.user.controller.dto.UserDto;

import java.time.LocalDateTime;

public record TodoDto(String title, Todo.Priority priority, String description, Boolean completed,
                      LocalDateTime dateCreated, LocalDateTime dateUpdated, UserDto user) {
}
