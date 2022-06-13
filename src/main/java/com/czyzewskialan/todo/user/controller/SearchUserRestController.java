package com.czyzewskialan.todo.user.controller;

import com.czyzewskialan.todo.user.controller.dto.UserDto;
import com.czyzewskialan.todo.user.domain.User;
import com.czyzewskialan.todo.user.service.SearchUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search/users")
@RequiredArgsConstructor
public class SearchUserRestController {

    private final SearchUserService searchService;

    @GetMapping
    public Page<UserDto> find(@RequestParam(value = "role", required = false) User.Role role, Pageable pageable) {
        return searchService.find(role, pageable);
    }
}
