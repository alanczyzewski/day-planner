package com.czyzewskialan.todo.user.controller;

import com.czyzewskialan.todo.user.controller.dto.UserToAddDto;
import com.czyzewskialan.todo.user.domain.User;
import com.czyzewskialan.todo.user.persistance.UserRepository;
import com.czyzewskialan.todo.user.service.UserService;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.util.NestedServletException;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import static com.czyzewskialan.todo.TestJsonUtils.convertObjectToJson;
import static com.czyzewskialan.todo.user.domain.User.DEFAULT_ROLE;
import static com.czyzewskialan.todo.user.service.UserService.LENGTH_RANDOM_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class UserControllerIntegrationTest {
    private static final String USERNAME_1 = "user1";
    private static final String USERNAME_2 = "admin";
    private static final String URL_BASE = "/users";
    private static final String URL_USER_1 = URL_BASE + "/" + USERNAME_1;
    private static final String URL_USER_1_ROLE = URL_USER_1 + "/role";
    private static final String URL_USER_1_PASSWORD = URL_USER_1 + "/password";
    private static final String URL_USER_1_PASSWORD_RESET = URL_USER_1_PASSWORD + "/reset";
    private static final String PASSWORD_HASH = "passwordHash";
    private static final String PASSWORD = "password";

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Mock
    private UserService mockUserService;
    private User user;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        user = User.builder().login(USERNAME_1).passwordHash(PASSWORD_HASH).role(User.Role.USER).build();
        mockMvc = standaloneSetup(new UserController(userService))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver()).build();
    }

    @AfterEach
    void tearDown() {
        if (userRepository.existsById(USERNAME_1)) {
            userRepository.deleteById(USERNAME_1);
        }
        if (userRepository.existsById(USERNAME_2)) {
            userRepository.deleteById(USERNAME_2);
        }
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldThrowAccessDeniedExceptionWhenPlainUserWantsToGetAllUsers() {
        //when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> mockMvc.perform(get(URL_BASE));

        //then
        assertThatAccessDeniedExceptionIsThrown(throwingCallable);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnUsersWithoutPassword() throws Exception {
        //given
        userRepository.save(user);
        User user2 = User.builder().login(USERNAME_2).passwordHash(PASSWORD_HASH).role(User.Role.ADMIN).build();
        userRepository.save(user2);

        //when + then
        mockMvc.perform(get(URL_BASE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0]", hasKey("dateCreated")))
                .andExpect(jsonPath("$.content[0]", hasKey("dateUpdated")))
                .andExpect(jsonPath("$.content[0].login", oneOf(USERNAME_1, USERNAME_2)))
                .andExpect(jsonPath("$.content[0].role", oneOf(User.Role.USER.name(), User.Role.ADMIN.name())))
                .andExpect(jsonPath("$.content[0].password").doesNotExist())
                .andExpect(jsonPath("$.content[0].passwordHash").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldThrowAccessDeniedExceptionWhenPlainUserWantsToGetUser() {
        //when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> mockMvc.perform(get(URL_USER_1));

        //then
        assertThatAccessDeniedExceptionIsThrown(throwingCallable);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenGettingNonexistentUser() throws Exception {
        //when + then
        mockMvc.perform(get(URL_USER_1))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnUserWithoutPassword() throws Exception {
        //given
        userRepository.save(user);

        //when + then
        mockMvc.perform(get(URL_USER_1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$", hasKey("dateCreated")))
                .andExpect(jsonPath("$", hasKey("dateUpdated")))
                .andExpect(jsonPath("$.login", is(USERNAME_1)))
                .andExpect(jsonPath("$.role", is(user.getRole().name())))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldThrowAccessDeniedExceptionWhenPlainUserWantsToCreateUser() throws IOException {
        //given
        UserToAddDto userToAdd = UserToAddDto.builder().login(USERNAME_1).password(PASSWORD).role(User.Role.ADMIN).build();
        MockHttpServletRequestBuilder content = post(URL_BASE)
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJson(userToAdd));

        //when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> mockMvc.perform(content);

        //then
        assertThatAccessDeniedExceptionIsThrown(throwingCallable);
    }

    @ParameterizedTest
    @MethodSource("userToAddWithMissingRequiredFields")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenCreatingUserWithMissingRequiredFields(UserToAddDto userToAdd) throws Exception {
        //given
        MockHttpServletRequestBuilder content = post(URL_BASE)
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJson(userToAdd));

        //when + then
        mockMvc.perform(content)
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateUserWithDefaultRoleAndReturn201() throws Exception {
        //given
        UserToAddDto userToAddWithoutRole = UserToAddDto.builder().login(USERNAME_1).password(PASSWORD).build();
        MockHttpServletRequestBuilder content = post(URL_BASE)
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJson(userToAddWithoutRole));

        //when + then
        mockMvc.perform(content)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$", hasKey("dateCreated")))
                .andExpect(jsonPath("$", hasKey("dateUpdated")))
                .andExpect(jsonPath("$.login", is(userToAddWithoutRole.getLogin())))
                .andExpect(jsonPath("$.role", is(DEFAULT_ROLE.name())))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        Optional<User> userDB = userRepository.findById(USERNAME_1);
        assertThat(userDB).isPresent();
        assertThat(userDB.get().getRole()).isEqualTo(DEFAULT_ROLE);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateUserAndReturn201() throws Exception {
        //given
        UserToAddDto userToAdd = UserToAddDto.builder().login(USERNAME_1).password(PASSWORD).role(User.Role.ADMIN).build();
        MockHttpServletRequestBuilder content = post(URL_BASE)
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJson(userToAdd));

        //when + then
        mockMvc.perform(content)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$", hasKey("dateCreated")))
                .andExpect(jsonPath("$", hasKey("dateUpdated")))
                .andExpect(jsonPath("$.login", is(userToAdd.getLogin())))
                .andExpect(jsonPath("$.role", is(userToAdd.getRole().name())))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        assertTrue(userRepository.existsById(userToAdd.getLogin()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn409WhenUserWithThisLoginAlreadyExists() throws Exception {
        //given
        userRepository.save(user);
        UserToAddDto userToAdd = UserToAddDto.builder().login(USERNAME_1).password(PASSWORD).role(User.Role.ADMIN).build();
        MockHttpServletRequestBuilder content = post(URL_BASE)
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJson(userToAdd));

        //when + then
        mockMvc.perform(content)
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldThrowAccessDeniedExceptionWhenPlainUserWantsToDeleteUser() {
        //when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> mockMvc.perform(delete(URL_USER_1));

        //then
        assertThatAccessDeniedExceptionIsThrown(throwingCallable);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenDeletingNonexistentUser() throws Exception {
        //when + then
        mockMvc.perform(delete(URL_USER_1))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteUserAndReturn200() throws Exception {
        //given
        userRepository.save(user);

        //when + then
        mockMvc.perform(delete(URL_USER_1))
                .andExpect(status().isOk());
        assertFalse(userRepository.existsById(USERNAME_1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldThrowAccessDeniedExceptionWhenPlainUserWantsToChangeRole() throws IOException {
        //given
        MockHttpServletRequestBuilder content = put(URL_USER_1_ROLE)
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJson(User.Role.ADMIN));

        //when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> mockMvc.perform(content);

        //then
        assertThatAccessDeniedExceptionIsThrown(throwingCallable);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenChangingRoleForNonexistentUser() throws Exception {
        //given
        MockHttpServletRequestBuilder content = put(URL_USER_1_ROLE)
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJson(User.Role.ADMIN));

        //when + then
        mockMvc.perform(content)
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400WhenRoleIsInvalid() throws Exception {
        //given
        MockHttpServletRequestBuilder content = put(URL_USER_1_ROLE)
                .contentType(APPLICATION_JSON)
                .content("invalidRole");

        //when + then
        mockMvc.perform(content)
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldChangeRoleAndReturn200() throws Exception {
        //given
        userRepository.save(user);
        User.Role newRole = User.Role.ADMIN;
        MockHttpServletRequestBuilder content = put(URL_USER_1_ROLE)
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJson(newRole));

        //when + then
        mockMvc.perform(content)
                .andExpect(status().isOk());
        Optional<User> userDB = userRepository.findById(USERNAME_1);
        assertThat(userDB).isPresent();
        assertThat(userDB.get().getRole()).isEqualTo(newRole);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldThrowAccessDeniedExceptionWhenPlainUserWantsToResetPassword() {
        //when + then
        ThrowableAssert.ThrowingCallable throwingCallable = () -> mockMvc.perform(get(URL_USER_1_PASSWORD_RESET));

        //then
        assertThatAccessDeniedExceptionIsThrown(throwingCallable);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenResettingPasswordForNonexistentUser() throws Exception {
        //when + then
        mockMvc.perform(get(URL_USER_1_PASSWORD_RESET))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn200WhenResettingPasswordSucceeded() throws Exception {
        //given
        userRepository.save(user);

        //when + then
        mockMvc.perform(get(URL_USER_1_PASSWORD_RESET))
                .andExpect(status().isOk())
                .andExpect(content().string(hasLength(LENGTH_RANDOM_PASSWORD)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenChangingPasswordForNonexistentUser() throws Exception {
        //given
        MockHttpServletRequestBuilder content = put(URL_USER_1_PASSWORD)
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJson("newSecretPassword"));
        doThrow(EntityNotFoundException.class)
                .when(mockUserService).changePassword(anyString(), anyString(), eq(null));
        mockMvc = standaloneSetup(new UserController(mockUserService))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver()).build();

        //when + then
        mockMvc.perform(content)
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn200WhenChangingPasswordSucceeded() throws Exception {
        //given
        MockHttpServletRequestBuilder content = put(URL_USER_1_PASSWORD)
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJson("newSecretPassword"));
        doNothing().when(mockUserService).changePassword(anyString(), anyString(), eq(null));
        mockMvc = standaloneSetup(new UserController(mockUserService))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver()).build();

        //when + then
        mockMvc.perform(content)
                .andExpect(status().isOk());
    }

    private void assertThatAccessDeniedExceptionIsThrown(ThrowableAssert.ThrowingCallable throwingCallable) {
        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(throwingCallable)
                .withRootCauseExactlyInstanceOf(AccessDeniedException.class);
    }

    private static Stream<UserToAddDto> userToAddWithMissingRequiredFields() {
        UserToAddDto userToAddWithoutLogin = UserToAddDto.builder().password(PASSWORD).role(User.Role.USER).build();
        UserToAddDto userToAddWithoutPassword = UserToAddDto.builder().login(USERNAME_1).role(User.Role.USER).build();
        UserToAddDto userToAddWithBlankLogin = UserToAddDto.builder().login("").password(PASSWORD).role(User.Role.USER).build();
        UserToAddDto userToAddWithBlankPassword = UserToAddDto.builder().login(USERNAME_1).password("").role(User.Role.USER).build();
//        UserToAddDto userToAddWithWrongLogin = UserToAddDto.builder().login("login with space").password(PASSWORD).role(User.Role.USER).build();
        //TODO add validation for login
//        UserToAddDto userToAddWithWrongPassword = UserToAddDto.builder().login(USERNAME_1).password("password with space").role(User.Role.USER).build();
        //TODO add validation for password

        return Stream.of(userToAddWithoutLogin, userToAddWithoutPassword, userToAddWithBlankLogin,
                userToAddWithBlankPassword); //, userToAddWithWrongLogin, userToAddWithWrongPassword);
    }
}