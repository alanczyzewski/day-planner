package com.czyzewskialan.todo.todo.controller.dto;

import com.czyzewskialan.todo.todo.domain.Todo;
import com.czyzewskialan.todo.user.controller.dto.User2UserDtoConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class Todo2TodoDtoConverter implements Function<Todo, TodoDto> {
    private final User2UserDtoConverter user2UserDtoConverter;

    @Override
    public TodoDto apply(Todo todo) {
        return new TodoDto(todo.getTitle(), todo.getPriority(), todo.getDescription(), todo.getCompleted(),
                todo.getDateCreated(), todo.getDateUpdated(), user2UserDtoConverter.apply(todo.getUser()));
    }
}
