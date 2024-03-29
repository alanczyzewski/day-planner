package com.czyzewskialan.todo.todo.domain;

import com.czyzewskialan.todo.user.domain.User;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "TODOS")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Todo implements Serializable {

    @Serial
    private static final long serialVersionUID = 7831108035613382655L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String title;

    private String description;

    private Boolean completed = false;

    private Priority priority;

    private LocalDateTime dateCreated;

    private LocalDateTime dateUpdated;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "username", nullable = false)
    private User user;

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

    public enum Priority {
        HIGH(0) {
            @Override
            public Priority increase() {
                return this;
            }
        },
        MEDIUM(1),
        LOW(2) {
            @Override
            public Priority decrease() {
                return this;
            }
        };

        @Getter
        private final int number;
        Priority(int number) {
            this.number = number;
        }

        public Priority increase() {
            return values()[ordinal() - 1];
        }

        public Priority decrease() {
            return values()[ordinal() + 1];
        }
    }
}
