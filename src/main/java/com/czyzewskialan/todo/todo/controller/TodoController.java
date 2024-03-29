package com.czyzewskialan.todo.todo.controller;

import com.czyzewskialan.todo.todo.controller.dto.TodoDto;
import com.czyzewskialan.todo.todo.controller.dto.TodoToAddDto;
import com.czyzewskialan.todo.todo.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @GetMapping
    public Page<TodoDto> getAll(Pageable pageRequest, Authentication auth) {
        return todoService.findAll(pageRequest, auth);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TodoDto create(@RequestBody TodoToAddDto todo, Authentication auth) {
        return todoService.create(todo, auth);
    }

    @GetMapping("/{id}")
    public TodoDto getOne(@PathVariable("id") Long id, Authentication auth) {
        return todoService.getOne(id, auth);
    }

    @PutMapping("/{id}")
    public TodoDto update(@RequestBody TodoToAddDto todo, @PathVariable("id") Long id, Authentication auth) {
        return todoService.update(todo, id, auth);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id, Authentication auth) {
        todoService.delete(id, auth);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public String notFoundHandler() {
        return "Todo not found";
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public String usernameNotFoundHandler(UsernameNotFoundException e) {
        return String.format("User \"%s\" does not exist.", e.getMessage());
    }
}
