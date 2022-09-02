package com.czyzewskialan.todo.todo.controller.dto;

import com.czyzewskialan.todo.todo.domain.Todo;

public record TodoToAddDto(String title, Todo.Priority priority, String description, Boolean completed) {
}
