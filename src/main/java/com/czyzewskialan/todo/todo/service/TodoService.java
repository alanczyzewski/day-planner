package com.czyzewskialan.todo.todo.service;

import com.czyzewskialan.todo.todo.controller.dto.Todo2TodoDtoConverter;
import com.czyzewskialan.todo.todo.controller.dto.TodoDto;
import com.czyzewskialan.todo.todo.domain.Todo;
import com.czyzewskialan.todo.todo.persistance.TodoRepository;
import com.czyzewskialan.todo.user.domain.User;
import com.czyzewskialan.todo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

import static com.czyzewskialan.todo.security.SecurityUtils.hasAccessToTodo;
import static com.czyzewskialan.todo.security.SecurityUtils.isAdminLoggedIn;

@Service
@RequiredArgsConstructor
@Slf4j
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserService userService;
    private final Todo2TodoDtoConverter todo2TodoDtoConverter;

    public Page<TodoDto> findAll(Pageable pageRequest, Authentication auth) {
        if (isAdminLoggedIn(auth)) {
            return todoRepository.findAll(pageRequest)
                    .map(todo2TodoDtoConverter);
        }
        User user = userService.getLoggedInUser(auth);
        return todoRepository.findByUser(user, pageRequest)
                .map(todo2TodoDtoConverter);
    }

    public TodoDto create(Todo todo, Authentication auth) {
        User user = userService.getLoggedInUser(auth);
        todo.setUser(user);
        todo.setId(null);
        Todo savedTodo = todoRepository.save(todo);
        log.info("Todo {} has been created.", savedTodo);
        return todo2TodoDtoConverter.apply(savedTodo);
    }

    public TodoDto getOne(Long id, Authentication auth) throws EntityNotFoundException {
        Todo todo = todoRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if (hasAccessToTodo(auth, todo)) {
            return todo2TodoDtoConverter.apply(todo);
        } else {
            throw new EntityNotFoundException();
        }
    }

    public TodoDto update(Todo todoToUpdate, Authentication auth) {
        Optional<Todo> foundTodo = todoRepository.findById(todoToUpdate.getId());
        if (foundTodo.isPresent() && hasAccessToTodo(auth, foundTodo.get())) {
            todoToUpdate.setUser(foundTodo.get().getUser());
            Todo savedTodo = todoRepository.save(todoToUpdate);
            log.info("Todo {} has been updated.", savedTodo);
            return todo2TodoDtoConverter.apply(savedTodo);
        } else {
            return create(todoToUpdate, auth);
        }
    }

    public void delete(Long id, Authentication auth) {
        Todo todo = todoRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if (hasAccessToTodo(auth, todo)) {
            todoRepository.deleteById(id);
            log.info("Todo {} has been removed from the database.", todo);
        } else {
            throw new EntityNotFoundException();
        }
    }
}

