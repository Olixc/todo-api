package com.olixc.todo.service;

import com.olixc.todo.entity.Todo;
import com.olixc.todo.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TodoService {

    @Autowired
    private TodoRepository todoRepository;

    // Get all todos
    public List<Todo> getAllTodos() {
        return todoRepository.findAll();
    }

    // Get todo by ID
    public Optional<Todo> getTodoById(Long id) {
        return todoRepository.findById(id);
    }

    // Create new todo
    public Todo createTodo(Todo todo) {
        return todoRepository.save(todo);
    }

    // Update existing todo
    public Todo updateTodo(Long id, Todo todoDetails) {
        Optional<Todo> optionalTodo = todoRepository.findById(id);

        if (optionalTodo.isPresent()) {
            Todo todo = optionalTodo.get();
            todo.setTitle(todoDetails.getTitle());
            todo.setDescription(todoDetails.getDescription());
            todo.setCompleted(todoDetails.isCompleted());
            return todoRepository.save(todo);
        }

        return null; // Todo not found
    }

    // Delete todo
    public boolean deleteTodo(Long id) {
        if (todoRepository.existsById(id)) {
            todoRepository.deleteById(id);
            return true;
        }
        return false; // Todo not found
    }

    // Get todos by completion status
    public List<Todo> getTodosByStatus(boolean completed) {
        return todoRepository.findByCompleted(completed);
    }

    // Search todos by title
    public List<Todo> searchTodosByTitle(String title) {
        return todoRepository.findByTitleContainingIgnoreCase(title);
    }

    // Get completed todos
    public List<Todo> getCompletedTodos() {
        return todoRepository.findByCompletedTrue();
    }

    // Get pending todos
    public List<Todo> getPendingTodos() {
        return todoRepository.findByCompletedFalse();
    }
}