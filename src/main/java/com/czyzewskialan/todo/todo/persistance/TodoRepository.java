package com.czyzewskialan.todo.todo.persistance;

import com.czyzewskialan.todo.todo.domain.Todo;
import com.czyzewskialan.todo.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long>, JpaSpecificationExecutor<Todo> {

    Page<Todo> findByUser(User user, Pageable pageable);
}
