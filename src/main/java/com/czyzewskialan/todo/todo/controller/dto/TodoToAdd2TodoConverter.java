package com.czyzewskialan.todo.todo.controller.dto;

import com.czyzewskialan.todo.todo.domain.Todo;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class TodoToAdd2TodoConverter implements Function<TodoToAddDto, Todo> {

    @Override
    public Todo apply(TodoToAddDto todoToAddDto) {
        return Todo.builder()
                .title(todoToAddDto.title())
                .priority(todoToAddDto.priority())
                .description(todoToAddDto.description())
                .completed(todoToAddDto.completed()).build();
    }
}
