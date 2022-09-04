package com.czyzewskialan.todo.user.controller;

import com.czyzewskialan.todo.user.controller.dto.UserDto;
import com.czyzewskialan.todo.user.controller.dto.UserToAddDto;
import com.czyzewskialan.todo.user.domain.User;
import com.czyzewskialan.todo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public Page<UserDto> getAll(Pageable pageRequest) {
        return userService.findAll(pageRequest);
    }

    @GetMapping("/{login}")
    public UserDto getOne(@PathVariable("login") String login) {
        return userService.findOne(login);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody @Valid UserToAddDto user) {
        return userService.create(user);
    }

    @DeleteMapping("/{login}")
    public void delete(@PathVariable("login") String login) {
        userService.delete(login);
    }

    @PutMapping("/{login}/role")
    public void changeRole(@RequestBody User.Role role, @PathVariable("login") String login) {
        userService.changeRole(login, role);
    }

    @GetMapping("/{login}/password/reset")
    public String resetPassword(@PathVariable("login") String login) {
        return userService.resetPassword(login);
    }

    @PutMapping("/{login}/password")
    public void changePassword(@RequestBody String newPassword, @PathVariable("login") String login,
                               Authentication auth) {
        userService.changePassword(login, newPassword, auth);
    }

    @ExceptionHandler(EntityExistsException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public String entityExistsHandler(EntityExistsException e) {
        return String.format("User \"%s\" already exists.", e.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public String entityNotFoundHandler(EntityNotFoundException e) {
        return String.format("User \"%s\" not found.", e.getMessage());
    }
}
