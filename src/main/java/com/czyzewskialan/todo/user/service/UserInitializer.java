package com.czyzewskialan.todo.user.service;

import com.czyzewskialan.todo.todo.domain.Todo;
import com.czyzewskialan.todo.todo.persistance.TodoRepository;
import com.czyzewskialan.todo.user.domain.User;
import com.czyzewskialan.todo.user.persistance.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
@Slf4j
public class UserInitializer {

    @Autowired
    public UserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, TodoRepository todoRepository) {
        User admin = User.builder().login("admin")
                .passwordHash(passwordEncoder.encode("BtHaPasswd"))
                .role(User.Role.ADMIN)
                .build();
        User plain = User.builder().login("henio")
                .passwordHash(passwordEncoder.encode("henio"))
                .role(User.Role.USER)
                .build();

        Stream.of(admin, plain)
                .forEach(user -> {
                    userRepository.save(user);
                    log.info("user added: {}", user);
                });

        Todo todoAdmin1 = Todo.builder()
                .title("Admin's todo 1")
                .user(admin)
                .priority(Todo.Priority.HIGH)
                .completed(true)
                .build();
        Todo todoAdmin2 = Todo.builder()
                .title("Admin's todo 2")
                .user(admin)
                .priority(Todo.Priority.LOW)
                .build();
        Todo todoUser1 = Todo.builder()
                .title("User's todo 1")
                .user(plain)
                .priority(Todo.Priority.HIGH)
                .completed(true)
                .build();
        Todo todoUser2 = Todo.builder()
                .title("User's todo 2")
                .user(plain)
                .priority(Todo.Priority.LOW)
                .build();

        Stream.of(todoAdmin1, todoAdmin2, todoUser1, todoUser2)
                .forEach(todo -> {
                    todoRepository.save(todo);
                    log.info("todo added: {}", todo);
                });
    }
}
