package com.czyzewskialan.todo.user.service;

import com.czyzewskialan.todo.user.controller.dto.User2UserDtoConverter;
import com.czyzewskialan.todo.user.controller.dto.UserDto;
import com.czyzewskialan.todo.user.controller.dto.UserToAdd;
import com.czyzewskialan.todo.user.domain.User;
import com.czyzewskialan.todo.user.persistance.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

import static com.czyzewskialan.todo.security.SecurityUtils.hasAccessToUser;

@Service
@RequiredArgsConstructor
public class UserService {
    static final String MESSAGE_ACCESS_DENIED_CHANGE_PASSWORD = "Cannot change other user's password";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final User2UserDtoConverter user2UserDtoConverter;

    public User getLoggedInUser(Authentication auth) {
        String username = ((UserDetails) auth.getPrincipal()).getUsername();
        return userRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserDto> findAll(Pageable pageRequest) {
        return userRepository.findAll(pageRequest)
                .map(user2UserDtoConverter);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserDto findOne(String login) {
        return userRepository.findById(login)
                .map(user2UserDtoConverter)
                .orElseThrow(() -> new EntityNotFoundException(login));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserDto create(UserToAdd userToAdd) {
        if (userRepository.existsById(userToAdd.getLogin())) {
            throw new EntityExistsException(userToAdd.getLogin());
        }

        User user = User.builder()
                .login(userToAdd.getLogin())
                .passwordHash(passwordEncoder.encode(userToAdd.getPassword()))
                .role(userToAdd.getRole()).build();
        User userSaved = userRepository.save(user);
        return user2UserDtoConverter.apply(userSaved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(String login) {
        if (userRepository.existsById(login)) {
            userRepository.deleteById(login);
        } else {
            throw new EntityNotFoundException(login);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void changeRole(String login, User.Role role) {
        User user = userRepository.findById(login)
                .orElseThrow(() -> new EntityNotFoundException(login));
        user.setRole(role);
        userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public String resetPassword(String login) {
        User user = userRepository.findById(login)
                .orElseThrow(() -> new EntityNotFoundException(login));
        String randomPassword = RandomStringUtils.randomAlphanumeric(10);
        user.setPasswordHash(passwordEncoder.encode(randomPassword));
        userRepository.save(user);
        return randomPassword;
    }

    public void changePassword(String login, String newPassword, Authentication auth) {
        if (!hasAccessToUser(auth, login)) {
            throw new AccessDeniedException(MESSAGE_ACCESS_DENIED_CHANGE_PASSWORD);
        }
        User user = userRepository.findById(login)
                .orElseThrow(() -> new EntityNotFoundException(login));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
