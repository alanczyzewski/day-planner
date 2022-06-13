package com.czyzewskialan.todo.todo.controller.dto;

import com.czyzewskialan.todo.todo.domain.Todo;
import com.czyzewskialan.todo.user.controller.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TodoDto {
    private final String title;
    private final Todo.Priority priority;
    private final String description;
    private final boolean completed;
    private final UserDto user;

    public TodoDto(Todo todo) {
        this.title = todo.getTitle();
        this.priority = todo.getPriority();
        this.description = todo.getDescription();
        this.completed = todo.isCompleted();
        this.user = new UserDto(todo.getUser());
    }
}
