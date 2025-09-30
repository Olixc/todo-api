package com.olixc.todo.controller;

import com.olixc.todo.entity.Todo;
import com.olixc.todo.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/todos")
@CrossOrigin(origins = "*")
@Tag(name = "Todo API", description = "RESTful API for managing todos")
public class TodoController {

    @Autowired
    private TodoService todoService;

    // GET /api/v1/todos - Get all todos
    @GetMapping
    @Operation(summary = "Get all todos", description = "Retrieve a list of all todos")
    public ResponseEntity<List<Todo>> getAllTodos() {
        List<Todo> todos = todoService.getAllTodos();
        return ResponseEntity.ok(todos);
    }

    // GET /api/v1/todos/{id} - Get todo by ID
    @GetMapping("/{id}")
    public ResponseEntity<Todo> getTodoById(@PathVariable Long id) {
        Optional<Todo> todo = todoService.getTodoById(id);
        if (todo.isPresent()) {
            return ResponseEntity.ok(todo.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /api/v1/todos - Create new todo
    @PostMapping
    @Operation(summary = "Create a new todo", description = "Create a new todo item")
    public ResponseEntity<Todo> createTodo(@RequestBody Todo todo) {
        Todo createdTodo = todoService.createTodo(todo);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTodo);
    }

    // PUT /api/v1/todos/{id} - Update todo
    @PutMapping("/{id}")
    public ResponseEntity<Todo> updateTodo(@PathVariable Long id, @RequestBody Todo todoDetails) {
        Todo updatedTodo = todoService.updateTodo(id, todoDetails);
        if (updatedTodo != null) {
            return ResponseEntity.ok(updatedTodo);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/v1/todos/{id} - Delete todo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id) {
        boolean deleted = todoService.deleteTodo(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /api/v1/todos/completed - Get completed todos
    @GetMapping("/completed")
    public ResponseEntity<List<Todo>> getCompletedTodos() {
        List<Todo> todos = todoService.getCompletedTodos();
        return ResponseEntity.ok(todos);
    }

    // GET /api/v1/todos/pending - Get pending todos
    @GetMapping("/pending")
    public ResponseEntity<List<Todo>> getPendingTodos() {
        List<Todo> todos = todoService.getPendingTodos();
        return ResponseEntity.ok(todos);
    }

    // GET /api/v1/todos/search?title=keyword - Search todos by title
    @GetMapping("/search")
    public ResponseEntity<List<Todo>> searchTodosByTitle(@RequestParam String title) {
        List<Todo> todos = todoService.searchTodosByTitle(title);
        return ResponseEntity.ok(todos);
    }
}