package com.czyzewskialan.todo.todo.service;

import com.czyzewskialan.todo.todo.controller.dto.TodoDto;
import com.czyzewskialan.todo.todo.domain.Todo;
import com.czyzewskialan.todo.todo.persistance.TodoRepository;
import com.czyzewskialan.todo.user.domain.User;
import com.czyzewskialan.todo.user.service.UserService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

import static com.czyzewskialan.todo.security.SecurityUtils.hasAccessToTodo;
import static com.czyzewskialan.todo.security.SecurityUtils.isAdminLoggedIn;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserService userService;

    public Page<TodoDto> findAll(Pageable pageRequest, Authentication auth) {
        if (isAdminLoggedIn(auth)) {
            return todoRepository.findAll(pageRequest)
                    .map(TodoDto::new);
        }
        User user = userService.getLoggedInUser(auth);
        return todoRepository.findByUser(user, pageRequest)
                .map(TodoDto::new);
    }

    public TodoDto create(Todo todo, Authentication auth) {
        User user = userService.getLoggedInUser(auth);
        todo.setUser(user);
        todo.setId(null);
        return new TodoDto(todoRepository.save(todo));
    }

    public TodoDto getOne(Long id, Authentication auth) throws EntityNotFoundException {
        Todo todo = todoRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if (hasAccessToTodo(auth, todo)) {
            return new TodoDto(todo);
        } else {
            throw new EntityNotFoundException();
        }
    }

    public void update(Todo todoToUpdate, Authentication auth) {
        Optional<Todo> foundTodo = todoRepository.findById(todoToUpdate.getId());
        if (foundTodo.isPresent() && hasAccessToTodo(auth, foundTodo.get())) {
            todoToUpdate.setUser(foundTodo.get().getUser());
            todoRepository.save(todoToUpdate);
        } else {
            create(todoToUpdate, auth);
        }
    }

    public void delete(Long id, Authentication auth) {
        Todo todo = todoRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if (hasAccessToTodo(auth, todo)) {
            todoRepository.deleteById(id);
        } else {
            throw new EntityNotFoundException();
        }
    }
}
