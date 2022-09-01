package com.czyzewskialan.todo.todo.service;

import com.czyzewskialan.todo.security.model.CurrentUser;
import com.czyzewskialan.todo.todo.controller.dto.Todo2TodoDtoConverter;
import com.czyzewskialan.todo.todo.controller.dto.TodoDto;
import com.czyzewskialan.todo.todo.domain.Todo;
import com.czyzewskialan.todo.todo.persistance.TodoRepository;
import com.czyzewskialan.todo.user.controller.dto.User2UserDtoConverter;
import com.czyzewskialan.todo.user.domain.User;
import com.czyzewskialan.todo.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class TodoServiceTest {
    private static final SimpleGrantedAuthority AUTHORITY_USER = new SimpleGrantedAuthority("ROLE_USER");
    private static final SimpleGrantedAuthority AUTHORITY_ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");
    private static final String USERNAME_PLAIN_USER = "plainUser";
    private static final String USERNAME_ADMIN = "admin";
    private static final String PASSWORD_HASH = "Hashh";
    private static final User USER_PLAIN = User.builder().login(USERNAME_PLAIN_USER).passwordHash(PASSWORD_HASH).role(User.Role.USER).build();
    private static final User USER_ADMIN = User.builder().login(USERNAME_ADMIN).passwordHash(PASSWORD_HASH).role(User.Role.ADMIN).build();
    private static final Long TODO_ID = 1500100900L;
    private static final String TITLE = "Title";
    private static final String DESCRIPTION = "Opis";
    private static final CurrentUser CURRENT_USER_PLAIN_USER = CurrentUser.builder().login(USERNAME_PLAIN_USER).passwordHash(PASSWORD_HASH).role("USER").build();
    private static final CurrentUser CURRENT_USER_ADMIN = CurrentUser.builder().login(USERNAME_ADMIN).passwordHash(PASSWORD_HASH).role("ADMIN").build();
    private static final String NEW_TITLE = "New Title";

    private AutoCloseable autoCloseable;
    @Mock
    private TodoRepository todoRepository;
    @Mock
    private UserService userService;
    private TodoService todoService;
    @Mock
    private Authentication authentication;
    @Mock
    private Pageable pageable;
    @Captor
    ArgumentCaptor<Todo> todoArgumentCaptor;

    @BeforeEach
    void setUp() {
        autoCloseable = openMocks(this);
        Todo2TodoDtoConverter todo2TodoDtoConverter = new Todo2TodoDtoConverter(new User2UserDtoConverter());
        todoService = new TodoService(todoRepository, userService, todo2TodoDtoConverter);
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    void shouldFindOnlyTodosAssignedToTheUserWhenLoggedInUserIsPlainUser() {
        //given
        doReturn(newArrayList(AUTHORITY_USER))
                .when(authentication).getAuthorities();
        when(userService.getLoggedInUser(authentication))
                .thenReturn(USER_PLAIN);
        when(todoRepository.findByUser(eq(USER_PLAIN), any()))
                .thenReturn(new PageImpl<>(newArrayList()));

        //when
        todoService.findAll(pageable, authentication);

        //then
        verify(todoRepository, never()).findAll(any(Pageable.class));
        verify(userService).getLoggedInUser(authentication);
        verify(todoRepository).findByUser(eq(USER_PLAIN), any(Pageable.class));
    }

    @Test
    void shouldFindAllTodosWhenLoggedInUserIsAdmin() {
        //given
        doReturn(newArrayList(AUTHORITY_ADMIN))
                .when(authentication).getAuthorities();
        when(todoRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(newArrayList()));

        //when
        todoService.findAll(pageable, authentication);

        //then
        verify(todoRepository).findAll(pageable);
        verify(userService, never()).getLoggedInUser(any(Authentication.class));
        verify(todoRepository, never()).findByUser(any(User.class), any(Pageable.class));
    }

    @Test
    void shouldCreateTodoWithoutIdAndWithCorrectUser() {
        //given
        Todo todoToAdd = Todo.builder().id(TODO_ID).title(TITLE).priority(Todo.Priority.MEDIUM)
                .description(DESCRIPTION).completed(false).user(USER_ADMIN).build();

        when(userService.getLoggedInUser(any(Authentication.class)))
                .thenReturn(USER_PLAIN);
        when(todoRepository.save(any(Todo.class)))
                .thenReturn(todoToAdd);

        //when
        todoService.create(todoToAdd, authentication);

        //then
        verify(userService).getLoggedInUser(authentication);
        verify(todoRepository).save(todoArgumentCaptor.capture());

        Todo todoRightBeforeSaving = todoArgumentCaptor.getValue();
        assertThat(todoRightBeforeSaving.getId()).isNull();
        assertThat(todoRightBeforeSaving.getUser().getLogin()).isEqualTo(USERNAME_PLAIN_USER);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenUserTriesToGetNonexistentTodo() {
        //given
        when(todoRepository.findById(TODO_ID))
                .thenReturn(Optional.empty());

        //when + then
        assertThrows(EntityNotFoundException.class,
                () -> todoService.getOne(TODO_ID, authentication));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenUserTriesToGetOtherUsersTodo() {
        //given
        Todo todo = Todo.builder().title(TITLE).user(USER_ADMIN).build();
        when(todoRepository.findById(TODO_ID))
                .thenReturn(Optional.of(todo));
        doReturn(newArrayList(AUTHORITY_USER))
                .when(authentication).getAuthorities();
        when(authentication.getPrincipal())
                .thenReturn(CURRENT_USER_PLAIN_USER);

        //when + then
        assertThrows(EntityNotFoundException.class,
                () -> todoService.getOne(TODO_ID, authentication));
    }

    @Test
    void shouldReturnTodoWhenTheUserTriesToGetExistingTodoAssignedToThem() {
        //given
        Todo todo = Todo.builder().title(TITLE).user(USER_PLAIN).build();
        when(todoRepository.findById(TODO_ID))
                .thenReturn(Optional.of(todo));
        doReturn(newArrayList(AUTHORITY_USER))
                .when(authentication).getAuthorities();
        when(authentication.getPrincipal())
                .thenReturn(CURRENT_USER_PLAIN_USER);

        //when
        TodoDto todoDto = todoService.getOne(TODO_ID, authentication);

        //then
        assertThat(todoDto.user().login()).isEqualTo(USERNAME_PLAIN_USER);
        assertThat(todoDto.title()).isEqualTo(TITLE);
    }

    @Test
    void shouldReturnTodoWhenTheAdminTriesToGetExistingTodoAssignedToOtherUser() {
        //given
        Todo todo = Todo.builder().title(TITLE).user(USER_PLAIN).build();
        when(todoRepository.findById(TODO_ID))
                .thenReturn(Optional.of(todo));
        doReturn(newArrayList(AUTHORITY_ADMIN))
                .when(authentication).getAuthorities();
        when(authentication.getPrincipal())
                .thenReturn(CURRENT_USER_ADMIN);

        //when
        TodoDto todoDto = todoService.getOne(TODO_ID, authentication);

        //then
        assertThat(todoDto.user().login()).isEqualTo(USERNAME_PLAIN_USER);
        assertThat(todoDto.title()).isEqualTo(TITLE);
    }

    @Test
    void shouldCreateNewTodoWhenTheUserTriesToUpdateNonexistentTodo() {
        //given
        when(todoRepository.findById(TODO_ID))
                .thenReturn(Optional.empty());
        Todo todoToUpdate = Todo.builder().id(TODO_ID).title(TITLE).priority(Todo.Priority.MEDIUM)
                .description(DESCRIPTION).completed(false).user(USER_ADMIN).build();

        when(userService.getLoggedInUser(any(Authentication.class)))
                .thenReturn(USER_PLAIN);
        when(todoRepository.save(any(Todo.class)))
                .thenReturn(todoToUpdate);

        //when
        todoService.update(todoToUpdate, authentication);

        //then
        verify(todoRepository).findById(TODO_ID);
        verify(userService).getLoggedInUser(authentication);
        verify(todoRepository).save(any(Todo.class));
    }

    @Test
    void shouldCreateNewTodoWhenTheUserTriesToUpdateTodoAssignedToOtherUser() {
        //given
        Todo todoToUpdate = Todo.builder().id(TODO_ID).title(TITLE).priority(Todo.Priority.MEDIUM)
                .description(DESCRIPTION).completed(false).user(USER_ADMIN).build();
        Todo todoOtherUser = Todo.builder().title(TITLE).user(USER_ADMIN).build();

        when(todoRepository.findById(TODO_ID))
                .thenReturn(Optional.of(todoOtherUser));
        doReturn(newArrayList(AUTHORITY_USER))
                .when(authentication).getAuthorities();
        when(authentication.getPrincipal())
                .thenReturn(CURRENT_USER_PLAIN_USER);
        when(userService.getLoggedInUser(any(Authentication.class)))
                .thenReturn(USER_PLAIN);
        when(todoRepository.save(any(Todo.class)))
                .thenReturn(todoToUpdate);

        //when
        todoService.update(todoToUpdate, authentication);

        //then
        verify(todoRepository).findById(TODO_ID);
        verify(userService).getLoggedInUser(authentication);
        verify(todoRepository).save(any(Todo.class));
    }

    @Test
    void shouldUpdateTodoWhenTheUserTriesToUpdateTodoAssignedToThem() {
        //given
        Todo todoToUpdate = Todo.builder().id(TODO_ID).title(NEW_TITLE).priority(Todo.Priority.MEDIUM)
                .description(DESCRIPTION).completed(false).user(USER_ADMIN).build();
        Todo todoBeforeUpdate = Todo.builder().id(TODO_ID).title(TITLE).priority(Todo.Priority.MEDIUM)
                .description(DESCRIPTION).completed(false).user(USER_PLAIN).build();

        when(todoRepository.findById(TODO_ID))
                .thenReturn(Optional.of(todoBeforeUpdate));
        doReturn(newArrayList(AUTHORITY_USER))
                .when(authentication).getAuthorities();
        when(authentication.getPrincipal())
                .thenReturn(CURRENT_USER_PLAIN_USER);
        when(todoRepository.save(any(Todo.class)))
                .thenReturn(todoToUpdate);

        //when
        todoService.update(todoToUpdate, authentication);

        //then
        verify(todoRepository).findById(TODO_ID);
        verify(userService, never()).getLoggedInUser(authentication);
        verify(todoRepository).save(todoArgumentCaptor.capture());
        Todo todoRightBeforeUpdate = todoArgumentCaptor.getValue();
        assertThat(todoRightBeforeUpdate.getUser()).isEqualTo(USER_PLAIN);
        assertThat(todoRightBeforeUpdate.getTitle()).isEqualTo(NEW_TITLE);
    }

    @Test
    void shouldUpdateTodoWhenTheAdminTriesToUpdateTodoAssignedToOtherUser() {
        //given
        Todo todoToUpdate = Todo.builder().id(TODO_ID).title(NEW_TITLE).priority(Todo.Priority.MEDIUM)
                .description(DESCRIPTION).completed(false).user(USER_ADMIN).build();
        Todo todoOtherUser = Todo.builder().title(TITLE).user(USER_PLAIN).build();

        when(todoRepository.findById(TODO_ID))
                .thenReturn(Optional.of(todoOtherUser));
        doReturn(newArrayList(AUTHORITY_ADMIN))
                .when(authentication).getAuthorities();
        when(authentication.getPrincipal())
                .thenReturn(CURRENT_USER_ADMIN);
        when(userService.getLoggedInUser(any(Authentication.class)))
                .thenReturn(USER_ADMIN);
        when(todoRepository.save(any(Todo.class)))
                .thenReturn(todoToUpdate);

        //when
        todoService.update(todoToUpdate, authentication);

        //then
        verify(todoRepository).findById(TODO_ID);
        verify(userService, never()).getLoggedInUser(authentication);
        verify(todoRepository).save(todoArgumentCaptor.capture());
        Todo todoRightBeforeUpdate = todoArgumentCaptor.getValue();
        assertThat(todoRightBeforeUpdate.getUser()).isEqualTo(USER_PLAIN);
        assertThat(todoRightBeforeUpdate.getTitle()).isEqualTo(NEW_TITLE);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenTheUserTriesToDeleteNonexistentTodo() {
        //given
        when(todoRepository.findById(TODO_ID))
                .thenReturn(Optional.empty());

        //when + then
        assertThrows(EntityNotFoundException.class,
                () -> todoService.delete(TODO_ID, authentication));
        verify(todoRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenTheUserTriesToDeleteTodoAssignedToOtherUser() {
        //given
        Todo todo = Todo.builder().title(TITLE).user(USER_ADMIN).build();
        when(todoRepository.findById(TODO_ID))
                .thenReturn(Optional.of(todo));
        doReturn(newArrayList(AUTHORITY_USER))
                .when(authentication).getAuthorities();
        when(authentication.getPrincipal())
                .thenReturn(CURRENT_USER_PLAIN_USER);

        //when + then
        assertThrows(EntityNotFoundException.class,
                () -> todoService.delete(TODO_ID, authentication));
        verify(todoRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldDeleteTodoWhenTheUserTriesToDeleteTodoAssignedToThem() {
        //given
        Todo todo = Todo.builder().title(TITLE).user(USER_PLAIN).build();
        when(todoRepository.findById(TODO_ID))
                .thenReturn(Optional.of(todo));
        doReturn(newArrayList(AUTHORITY_USER))
                .when(authentication).getAuthorities();
        when(authentication.getPrincipal())
                .thenReturn(CURRENT_USER_PLAIN_USER);

        //when
        todoService.delete(TODO_ID, authentication);

        //then
        verify(todoRepository).deleteById(TODO_ID);
    }

    @Test
    void shouldDeleteTodoWhenTheAdminTriesToDeleteTodoAssignedToOtherUser() {
        //given
        Todo todo = Todo.builder().title(TITLE).user(USER_PLAIN).build();
        when(todoRepository.findById(TODO_ID))
                .thenReturn(Optional.of(todo));
        doReturn(newArrayList(AUTHORITY_ADMIN))
                .when(authentication).getAuthorities();
        when(authentication.getPrincipal())
                .thenReturn(CURRENT_USER_ADMIN);

        //when
        todoService.delete(TODO_ID, authentication);

        //then
        verify(todoRepository).deleteById(TODO_ID);
    }
}