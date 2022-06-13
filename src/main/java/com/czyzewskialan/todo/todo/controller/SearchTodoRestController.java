package com.czyzewskialan.todo.todo.controller;

import com.czyzewskialan.todo.todo.controller.dto.TodoDto;
import com.czyzewskialan.todo.todo.controller.dto.TodoSearchParamsDto;
import com.czyzewskialan.todo.todo.domain.Todo;
import com.czyzewskialan.todo.todo.service.SearchTodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search/todos")
@RequiredArgsConstructor
public class SearchTodoRestController {

    private final SearchTodoService searchService;

    @GetMapping
    public Page<TodoDto> find(@RequestParam(value = "title", required = false) String title,
                              @RequestParam(value = "priority", required = false) Todo.Priority priority,
                              @RequestParam(value = "completed", required = false) Boolean completed,
                              Pageable pageRequest, Authentication auth) {
        TodoSearchParamsDto todoSearchParamsDto = new TodoSearchParamsDto(title, priority, completed, pageRequest);
        return searchService.find(todoSearchParamsDto, auth);
    }
}
