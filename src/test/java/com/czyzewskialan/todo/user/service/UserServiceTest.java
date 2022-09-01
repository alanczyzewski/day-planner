package com.czyzewskialan.todo.user.service;

import com.czyzewskialan.todo.security.model.CurrentUser;
import com.czyzewskialan.todo.user.controller.dto.User2UserDtoConverter;
import com.czyzewskialan.todo.user.controller.dto.UserDto;
import com.czyzewskialan.todo.user.controller.dto.UserToAdd;
import com.czyzewskialan.todo.user.domain.User;
import com.czyzewskialan.todo.user.persistance.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

import static com.czyzewskialan.todo.user.service.UserService.MESSAGE_ACCESS_DENIED_CHANGE_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class UserServiceTest {
    private static final String USERNAME_1 = "user1";
    private static final String USERNAME_2 = "user2";
    private static final String PASSWORD_HASH = "someHash";
    private static final CurrentUser CURRENT_USER = CurrentUser.builder()
            .login(USERNAME_1).passwordHash(PASSWORD_HASH).role("USER").build();
    private static final CurrentUser CURRENT_USER_ADMIN = CurrentUser.builder()
            .login(USERNAME_2).passwordHash(PASSWORD_HASH).role("ADMIN").build();
    private User user;
    private User admin;

    private AutoCloseable autoCloseable;
    @Mock
    UserRepository userRepository;
    @Mock
    private Authentication authentication;
    private UserService userService;
    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private Pageable pageable;
    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @BeforeEach
    void setUp() {
        user = User.builder().login(USERNAME_1).role(User.Role.USER).passwordHash(PASSWORD_HASH).build();
        admin = User.builder().login(USERNAME_2).role(User.Role.ADMIN).build();
        autoCloseable = openMocks(this);
        userService = new UserService(userRepository, passwordEncoder, new User2UserDtoConverter());
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    void shouldInvokeRepositoryMethodWithCorrectUsernameAndReturnCorrectUserWhenUserExists() {
        //given
        when(userRepository.findById(USERNAME_1))
                .thenReturn(Optional.of(user));
        when(authentication.getPrincipal())
                .thenReturn(CURRENT_USER);

        //when
        User loggedInUser = userService.getLoggedInUser(authentication);

        //then
        verify(userRepository).findById(stringArgumentCaptor.capture());
        assertThat(stringArgumentCaptor.getValue()).isEqualTo(USERNAME_1);
        assertThat(loggedInUser).isEqualTo(user);
    }

    @Test
    void shouldThrowUsernameNotFoundExceptionWhenUserDoesNotExist() {
        //given
        when(authentication.getPrincipal())
                .thenReturn(CURRENT_USER);
        when(userRepository.findById(USERNAME_1))
                .thenReturn(Optional.empty());

        //when + then
        assertThrows(UsernameNotFoundException.class,
                () -> userService.getLoggedInUser(authentication),
                USERNAME_1);
    }

    @Test
    void shouldReturnAllUsers() {
        //given
        when(userRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(newArrayList(user, admin)));

        //when
        Page<UserDto> usersDtoPage = userService.findAll(pageable);

        //then
        verify(userRepository).findAll(pageable);
        List<UserDto> usersDto = usersDtoPage.getContent();
        assertThat(usersDto)
                .hasSize(2)
                .extracting("login")
                .contains(USERNAME_1, USERNAME_2);
    }

    @Test
    void shouldReturnCorrectUser() {
        //given
        when(userRepository.findById(USERNAME_1))
                .thenReturn(Optional.of(user));

        //when
        UserDto userDto = userService.findOne(USERNAME_1);

        //then
        verify(userRepository).findById(USERNAME_1);
        assertThat(userDto.getLogin()).isEqualTo(USERNAME_1);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhileGettingUserWhenUserDoesNotExist() {
        //given
        when(userRepository.findById(USERNAME_1))
                .thenReturn(Optional.empty());

        //when + then
        assertThrows(EntityNotFoundException.class,
                () -> userService.findOne(USERNAME_1),
                USERNAME_1);
    }

    @Test
    void shouldThrowEntityExistsExceptionWhenUserAlreadyExists() {
        //given
        UserToAdd userToAdd = UserToAdd.builder().login(USERNAME_1).build();
        when(userRepository.existsById(USERNAME_1))
                .thenReturn(true);

        //when + then
        assertThrows(EntityExistsException.class,
                () -> userService.create(userToAdd),
                USERNAME_1);
    }

    @Test
    void shouldSaveNewUser() {
        //given
        UserToAdd userToAdd = UserToAdd.builder().login(USERNAME_1).build();
        when(userRepository.existsById(USERNAME_1))
                .thenReturn(false);
        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        //when
        UserDto userDto = userService.create(userToAdd);

        //then
        verify(userRepository).save(any(User.class));
        assertThat(userDto.getLogin()).isEqualTo(USERNAME_1);
    }

    @Test
    void shouldDeleteUserWhenExists() {
        //given
        when(userRepository.existsById(USERNAME_1))
                .thenReturn(true);

        //when
        userService.delete(USERNAME_1);

        //then
        verify(userRepository).deleteById(USERNAME_1);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhileRemovingUserWhenUserDoesNotExist() {
        //given
        when(userRepository.existsById(USERNAME_1))
                .thenReturn(false);

        //when + then
        assertThrows(EntityNotFoundException.class,
                () -> userService.delete(USERNAME_1),
                USERNAME_1);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhileChangingRoleWhenUserDoesNotExist() {
        //given
        when(userRepository.findById(USERNAME_1))
                .thenReturn(Optional.empty());

        //when + then
        assertThrows(EntityNotFoundException.class,
                () -> userService.changeRole(USERNAME_1, User.Role.ADMIN),
                USERNAME_1);
    }

    @Test
    void shouldChangeRoleForUser() {
        //given
        when(userRepository.findById(USERNAME_1))
                .thenReturn(Optional.of(user));

        //when
        userService.changeRole(USERNAME_1, User.Role.ADMIN);

        //then
        verify(userRepository).save(userArgumentCaptor.capture());
        assertThat(userArgumentCaptor.getValue().getRole()).isEqualTo(User.Role.ADMIN);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhileResettingPasswordWhenUserDoesNotExist() {
        //given
        when(userRepository.findById(USERNAME_1))
                .thenReturn(Optional.empty());

        //when + then
        assertThrows(EntityNotFoundException.class,
                () -> userService.resetPassword(USERNAME_1),
                USERNAME_1);
    }

    @Test
    void shouldResetPassword() {
        //given
        doAnswer(invocationOnMock -> invocationOnMock.getArgument(0))
                .when(passwordEncoder).encode(any(CharSequence.class));
        when(userRepository.findById(USERNAME_1))
                .thenReturn(Optional.of(user));
        String passwordHashBefore = user.getPasswordHash();

        //when
        userService.resetPassword(USERNAME_1);

        //then
        verify(userRepository).save(userArgumentCaptor.capture());
        assertThat(userArgumentCaptor.getValue().getPasswordHash())
                .isNotNull()
                .isNotEqualTo(passwordHashBefore);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenUserWantsToChangePasswordForOtherUser() {
        //given
        when(authentication.getPrincipal())
                .thenReturn(CURRENT_USER);

        //when + then
        assertThrows(AccessDeniedException.class,
                () -> userService.changePassword(USERNAME_2, "newPassword", authentication),
                MESSAGE_ACCESS_DENIED_CHANGE_PASSWORD);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhileChangingPasswordWhenUserDoesNotExist() {
        //given
        when(userRepository.findById(USERNAME_1))
                .thenReturn(Optional.empty());
        when(authentication.getPrincipal())
                .thenReturn(CURRENT_USER);

        //when + then
        assertThrows(EntityNotFoundException.class,
                () -> userService.changePassword(USERNAME_1, "newPassword", authentication),
                USERNAME_1);
    }

    @Test
    void shouldChangeUserOwnPassword() {
        //given
        doAnswer(invocationOnMock -> invocationOnMock.getArgument(0))
                .when(passwordEncoder).encode(any(CharSequence.class));
        when(userRepository.findById(USERNAME_1))
                .thenReturn(Optional.of(user));
        when(authentication.getPrincipal())
                .thenReturn(CURRENT_USER);
        String newPassword = "newPassword";

        //when
        userService.changePassword(USERNAME_1, newPassword, authentication);

        //then
        verify(userRepository).save(userArgumentCaptor.capture());
        assertThat(userArgumentCaptor.getValue().getPasswordHash())
                .isEqualTo(newPassword);
    }

    @Test
    void shouldChangeOtherUserPasswordWhenAdminIsLoggedIn() {
        //given
        doReturn(newArrayList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .when(authentication).getAuthorities();
        doAnswer(invocationOnMock -> invocationOnMock.getArgument(0))
                .when(passwordEncoder).encode(any(CharSequence.class));
        when(userRepository.findById(USERNAME_1))
                .thenReturn(Optional.of(user));
        when(authentication.getPrincipal())
                .thenReturn(CURRENT_USER_ADMIN);
        String newPassword = "newPassword";

        //when
        userService.changePassword(USERNAME_1, newPassword, authentication);

        //then
        verify(userRepository).save(userArgumentCaptor.capture());
        assertThat(userArgumentCaptor.getValue().getPasswordHash())
                .isEqualTo(newPassword);
    }
}