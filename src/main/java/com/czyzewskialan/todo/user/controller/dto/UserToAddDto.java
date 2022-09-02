package com.czyzewskialan.todo.user.controller.dto;

import com.czyzewskialan.todo.user.domain.User;
import lombok.*;
import org.hibernate.validator.constraints.NotEmpty;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserToAddDto {

    @NotEmpty
    private String login;

    @NotEmpty
    private String password;

    private User.Role role = User.Role.USER;
}
