package com.czyzewskialan.todo.user.controller.dto;

import com.czyzewskialan.todo.user.domain.User;
import lombok.*;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserToAddDto {

    @NotBlank
    private String login;

    @NotBlank
    private String password;

    private User.Role role = User.Role.USER;
}
