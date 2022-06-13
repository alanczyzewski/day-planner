package com.czyzewskialan.todo.todo.controller.dto;

import com.czyzewskialan.todo.todo.domain.Todo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

@AllArgsConstructor
@Getter
public class TodoSearchParamsDto {

    private final String title;
    private final Todo.Priority priority;
    private final Boolean completed;
    private final Pageable pageRequest;
}
