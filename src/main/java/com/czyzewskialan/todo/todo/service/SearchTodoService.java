package com.czyzewskialan.todo.todo.service;

import com.czyzewskialan.todo.todo.controller.dto.Todo2TodoDtoConverter;
import com.czyzewskialan.todo.todo.controller.dto.TodoDto;
import com.czyzewskialan.todo.todo.controller.dto.TodoSearchParamsDto;
import com.czyzewskialan.todo.todo.domain.Todo;
import com.czyzewskialan.todo.todo.persistance.TodoRepository;
import com.czyzewskialan.todo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import static com.czyzewskialan.todo.security.SecurityUtils.isAdminLoggedIn;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Objects.nonNull;
import static org.springframework.data.jpa.domain.Specification.where;

@Service
@RequiredArgsConstructor
public class SearchTodoService {

    private final TodoRepository todoRepository;
    private final UserService userService;
    private final Todo2TodoDtoConverter todo2TodoDtoConverter;

    public Page<TodoDto> find(TodoSearchParamsDto searchParams, Authentication auth) {
        return todoRepository.findAll(
                        where(getSpecificationUser(auth))
                                .and(getSpecificationTitle(searchParams.getTitle()))
                                .and(getSpecificationPriority(searchParams.getPriority()))
                                .and(getSpecificationCompleted(searchParams.getCompleted())),
                        searchParams.getPageRequest())
                .map(todo2TodoDtoConverter);
    }

    private Specification<Todo> getSpecificationUser(Authentication auth) {
        if (!isAdminLoggedIn(auth)) {
            return (root, query, builder) -> builder.equal(root.get("user"), userService.getLoggedInUser(auth));
        } else {
            return null;
        }
    }

    private Specification<Todo> getSpecificationTitle(String title) {
        if (!isNullOrEmpty(title)) {
            return (root, query, builder) -> builder.equal(root.get("title"), title);
        } else {
            return null;
        }
    }

    private Specification<Todo> getSpecificationPriority(Todo.Priority priority) {
        if (nonNull(priority)) {
            return (root, query, builder) -> builder.equal(root.get("priority"), priority);
        } else {
            return null;
        }
    }

    private Specification<Todo> getSpecificationCompleted(Boolean completed) {
        if (nonNull(completed)) {
            return (root, query, builder) -> builder.equal(root.get("completed"), completed);
        } else {
            return null;
        }
    }
}
