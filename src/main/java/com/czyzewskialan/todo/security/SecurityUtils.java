package com.czyzewskialan.todo.security;

import com.czyzewskialan.todo.todo.domain.Todo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtils {
    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    public static boolean isAdminLoggedIn(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(ADMIN_ROLE));
    }

    public static boolean hasAccessToTodo(Authentication auth, Todo todo) {
        return hasAccess(auth, todo.getUser().getLogin());
    }

    public static boolean hasAccessToUser(Authentication auth, String username) {
        return hasAccess(auth, username);
    }

    private static boolean hasAccess(Authentication auth, String username) {
        return isAdminLoggedIn(auth) ||
                ((UserDetails) auth.getPrincipal()).getUsername().equals(username);
    }
}