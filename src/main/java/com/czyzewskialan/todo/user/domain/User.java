package com.czyzewskialan.todo.user.domain;

import com.czyzewskialan.todo.todo.domain.Todo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
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
    @Id
    @NotEmpty
    private String login;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotEmpty
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Role role;

    private LocalDateTime dateCreated;

    private LocalDateTime dateUpdated;

    @ToString.Exclude
    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST)
    private List<Todo> todos;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        dateCreated = now;
        dateUpdated = now;
    }

    @PreUpdate
    void preUpdate() {
        dateUpdated = LocalDateTime.now();
    }

    public enum Role {
        USER, ADMIN
    }

}
