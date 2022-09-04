package com.czyzewskialan.todo.user.domain;

import com.czyzewskialan.todo.todo.domain.Todo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "USERS")
public class User {
    public static final User.Role DEFAULT_ROLE = User.Role.USER;

    @Id
    @NotBlank
    private String login;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    @NotNull
    private LocalDateTime dateCreated;

    @NotNull
    private LocalDateTime dateUpdated;

    @ToString.Exclude
    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST)
    private List<Todo> todos;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        dateCreated = now;
        dateUpdated = now;
        role = role != null ? role : DEFAULT_ROLE;
    }

    @PreUpdate
    void preUpdate() {
        dateUpdated = LocalDateTime.now();
    }

    public enum Role {
        USER, ADMIN
    }

}
