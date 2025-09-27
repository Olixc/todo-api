package com.olixc.todo.repository;

import com.olixc.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    
    // Find todos by completion status
    List<Todo> findByCompleted(boolean completed);

    // Find todos by title containing text
    List<Todo> findByTitleContainingIgnoreCase(String title);

    // Find completed todos
    List<Todo> findByCompletedTrue();

    // Find pending todos
    List<Todo> findByCompletedFalse();
}