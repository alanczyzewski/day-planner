package com.czyzewskialan.todo.todo.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TodoTest {
    @ParameterizedTest
    @MethodSource("argumentsIncreasePriority")
    void shouldIncreasePriorityIfItIsPossible(Todo.Priority current, Todo.Priority expected) {
        //when
        Todo.Priority increased = current.increase();

        //then
        assertThat(increased).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("argumentsDecreasePriority")
    void shouldDecreasePriorityIfItIsPossible(Todo.Priority current, Todo.Priority expected) {
        //when
        Todo.Priority decreased = current.decrease();

        //then
        assertThat(decreased).isEqualTo(expected);
    }

    private static Stream<Arguments> argumentsIncreasePriority() {
        return Stream.of(
                Arguments.of(Todo.Priority.LOW, Todo.Priority.MEDIUM),
                Arguments.of(Todo.Priority.MEDIUM, Todo.Priority.HIGH),
                Arguments.of(Todo.Priority.HIGH, Todo.Priority.HIGH)
        );
    }

    private static Stream<Arguments> argumentsDecreasePriority() {
        return Stream.of(
                Arguments.of(Todo.Priority.LOW, Todo.Priority.LOW),
                Arguments.of(Todo.Priority.MEDIUM, Todo.Priority.LOW),
                Arguments.of(Todo.Priority.HIGH, Todo.Priority.MEDIUM)
        );
    }
}