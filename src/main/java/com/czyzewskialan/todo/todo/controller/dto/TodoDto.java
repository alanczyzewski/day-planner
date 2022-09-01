package com.czyzewskialan.todo.todo.controller.dto;

import com.czyzewskialan.todo.todo.domain.Todo;
import com.czyzewskialan.todo.user.controller.dto.UserDto;

public record TodoDto(String title, Todo.Priority priority, String description, boolean completed, UserDto user) {
}
