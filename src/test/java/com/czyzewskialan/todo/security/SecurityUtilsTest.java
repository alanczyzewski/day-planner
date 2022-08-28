package com.czyzewskialan.todo.security;

import com.czyzewskialan.todo.security.model.CurrentUser;
import com.czyzewskialan.todo.todo.domain.Todo;
import com.czyzewskialan.todo.user.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class SecurityUtilsTest {
    private static final SimpleGrantedAuthority AUTHORITY_USER = new SimpleGrantedAuthority("ROLE_USER");
    private static final SimpleGrantedAuthority AUTHORITY_ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");
    private static final String USERNAME_PLAIN_USER = "plainUser";
    private static final String USERNAME_ADMIN = "admin";
    private static final String PASSWORD_HASH = "Hashh";
    private static final CurrentUser CURRENT_USER_PLAIN_USER = CurrentUser.builder().login(USERNAME_PLAIN_USER).passwordHash(PASSWORD_HASH).role("USER").build();
    private static final CurrentUser CURRENT_USER_ADMIN = CurrentUser.builder().login(USERNAME_ADMIN).passwordHash(PASSWORD_HASH).role("ADMIN").build();

    private AutoCloseable autoCloseable;
    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        autoCloseable = openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @ParameterizedTest(name = "[{index}] Roles: {0}. Is admin logged in: {1}.")
    @MethodSource("argumentsIsAdminLoggedIn")
    void shouldDecideIfAdminIsLoggedIn(List<SimpleGrantedAuthority> authorities, boolean expectedResult) {
        //given
        doReturn(authorities)
                .when(authentication).getAuthorities();

        //when
        boolean result = SecurityUtils.isAdminLoggedIn(authentication);

        //then
        assertThat(result).isEqualTo(expectedResult);
    }

    @ParameterizedTest(name = "[{index}] {0} tries to get {2}. Access: {3}.")
    @MethodSource("argumentsHasAccessToTodo")
    void shouldDecideIfUserHasAccessToTodo(CurrentUser currentUser, SimpleGrantedAuthority authority, Todo todo, boolean expectedResult) {
        //given
        doReturn(newArrayList(authority))
                .when(authentication).getAuthorities();
        when(authentication.getPrincipal())
                .thenReturn(currentUser);

        //when
        boolean result = SecurityUtils.hasAccessToTodo(authentication, todo);

        //then
        assertThat(result).isEqualTo(expectedResult);
    }

    @ParameterizedTest(name = "[{index}] {0} tries to get user named \"{2}\". Access: {3}.")
    @MethodSource("argumentsHasAccessToUser")
    void shouldDecideIfUserHasAccessToUser(CurrentUser currentUser, SimpleGrantedAuthority authority, String username, boolean expectedResult) {
        //given
        doReturn(newArrayList(authority))
                .when(authentication).getAuthorities();
        when(authentication.getPrincipal())
                .thenReturn(currentUser);

        //when
        boolean result = SecurityUtils.hasAccessToUser(authentication, username);

        //then
        assertThat(result).isEqualTo(expectedResult);
    }

    private static Stream<Arguments> argumentsIsAdminLoggedIn() {
        return Stream.of(
                Arguments.of(newArrayList(AUTHORITY_USER), false),
                Arguments.of(newArrayList(AUTHORITY_ADMIN), true),
                Arguments.of(newArrayList(AUTHORITY_USER, AUTHORITY_ADMIN), true),
                Arguments.of(newArrayList(AUTHORITY_ADMIN, AUTHORITY_USER), true));
    }

    private static Stream<Arguments> argumentsHasAccessToTodo() {
        User userPlain = User.builder().login(USERNAME_PLAIN_USER).passwordHash(PASSWORD_HASH).role(User.Role.USER).build();
        Todo todoPlainUser = Todo.builder().user(userPlain).build();
        User userAdmin = User.builder().login(USERNAME_ADMIN).passwordHash(PASSWORD_HASH).role(User.Role.ADMIN).build();
        Todo todoAdminUser = Todo.builder().user(userAdmin).build();
        return Stream.of(
                Arguments.of(CURRENT_USER_PLAIN_USER, AUTHORITY_USER, todoAdminUser, false),
                Arguments.of(CURRENT_USER_PLAIN_USER, AUTHORITY_USER, todoPlainUser, true),
                Arguments.of(CURRENT_USER_ADMIN, AUTHORITY_ADMIN, todoAdminUser, true),
                Arguments.of(CURRENT_USER_ADMIN, AUTHORITY_ADMIN, todoPlainUser, true));
    }

    private static Stream<Arguments> argumentsHasAccessToUser() {
        return Stream.of(
                Arguments.of(CURRENT_USER_PLAIN_USER, AUTHORITY_USER, USERNAME_ADMIN, false),
                Arguments.of(CURRENT_USER_PLAIN_USER, AUTHORITY_USER, USERNAME_PLAIN_USER, true),
                Arguments.of(CURRENT_USER_ADMIN, AUTHORITY_ADMIN, USERNAME_ADMIN, true),
                Arguments.of(CURRENT_USER_ADMIN, AUTHORITY_ADMIN, USERNAME_PLAIN_USER, true));
    }
}