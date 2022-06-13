package com.czyzewskialan.todo.user.service;

import com.czyzewskialan.todo.user.controller.dto.UserDto;
import com.czyzewskialan.todo.user.domain.User;
import com.czyzewskialan.todo.user.persistance.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SearchUserService {

    private final UserRepository repository;

    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserDto> find(User.Role role, Pageable pageable) {
        if (Objects.isNull(role)) {
            return repository.findAll(pageable)
                    .map(UserDto::new);
        } else {
            return repository.findByRole(role, pageable)
                    .map(UserDto::new);
        }
    }

}
