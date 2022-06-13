package com.czyzewskialan.todo.user.persistance;

import com.czyzewskialan.todo.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    Page<User> findByRole(User.Role role, Pageable pageable);
}
