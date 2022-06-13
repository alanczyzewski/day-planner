package com.czyzewskialan.todo.user.service;

import com.czyzewskialan.todo.user.controller.dto.User2UserDtoConverter;
import com.czyzewskialan.todo.user.controller.dto.UserDto;
import com.czyzewskialan.todo.user.controller.dto.UserToAdd;
import com.czyzewskialan.todo.user.domain.User;
import com.czyzewskialan.todo.user.persistance.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

import static com.czyzewskialan.todo.security.SecurityUtils.hasAccessToUser;

@Service
public class UserService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private final User2UserDtoConverter converter;

    public UserService() {
        this.converter = new User2UserDtoConverter();
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public User getLoggedInUser(Authentication auth) {
        String username = ((UserDetails) auth.getPrincipal()).getUsername();
        return userRepository.findById(username).orElse(null);//TODO
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserDto> findAll(Pageable pageRequest) {
        return userRepository.findAll(pageRequest)
                .map(converter);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserDto findOne(String login) {
        User user = userRepository.findById(login).orElseThrow(EntityNotFoundException::new);
        return new UserDto(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserDto create(UserToAdd userToAdd) {
        if (userRepository.existsById(userToAdd.getLogin())) {
            throw new EntityExistsException();
        }

        User user = User.builder()
                .login(userToAdd.getLogin())
                .passwordHash(passwordEncoder.encode(userToAdd.getPassword()))
                .role(userToAdd.getRole()).build();

        User userSaved = userRepository.save(user);

        return new UserDto(userSaved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(String login) {
        if (userRepository.existsById(login)) {
            userRepository.deleteById(login);
        } else {
            throw new EntityNotFoundException();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void changeRole(String login, User.Role role) {
        User user = userRepository.findById(login).orElseThrow(EntityNotFoundException::new);
        user.setRole(role);
        userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public String resetPassword(String login) {
        User user = userRepository.findById(login).orElseThrow(EntityNotFoundException::new);
        String randomPassword = RandomStringUtils.randomAlphanumeric(10);
        user.setPasswordHash(passwordEncoder.encode(randomPassword));
        userRepository.save(user);
        return randomPassword;
    }

    public void changePassword(String login, String newPassword, Authentication auth) {
        if (!hasAccessToUser(auth, login)) {
            throw new AccessDeniedException("Access denied");
        }
        User user = userRepository.findById(login).orElseThrow(EntityNotFoundException::new);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
