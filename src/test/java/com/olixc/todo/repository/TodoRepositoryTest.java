package com.olixc.todo.repository;

import com.olixc.todo.entity.Todo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TodoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TodoRepository todoRepository;

    private Todo testTodo1;
    private Todo testTodo2;
    private Todo testTodo3;

    @BeforeEach
    void setUp() {
        // Create test todos
        testTodo1 = new Todo();
        testTodo1.setTitle("Learn Spring Boot");
        testTodo1.setDescription("Build a REST API with Spring Boot");
        testTodo1.setCompleted(false);
        testTodo1.setCreatedAt(LocalDateTime.now());
        testTodo1.setUpdatedAt(LocalDateTime.now());

        testTodo2 = new Todo();
        testTodo2.setTitle("Deploy to Kubernetes");
        testTodo2.setDescription("Deploy the todo API to K8s cluster");
        testTodo2.setCompleted(true);
        testTodo2.setCreatedAt(LocalDateTime.now());
        testTodo2.setUpdatedAt(LocalDateTime.now());

        testTodo3 = new Todo();
        testTodo3.setTitle("Spring Security");
        testTodo3.setDescription("Add authentication to the API");
        testTodo3.setCompleted(false);
        testTodo3.setCreatedAt(LocalDateTime.now());
        testTodo3.setUpdatedAt(LocalDateTime.now());

        // Persist test data
        entityManager.persistAndFlush(testTodo1);
        entityManager.persistAndFlush(testTodo2);
        entityManager.persistAndFlush(testTodo3);
    }

    @Test
    void findAll_ShouldReturnAllTodos() {
        // When
        List<Todo> todos = todoRepository.findAll();

        // Then
        assertNotNull(todos);
        assertEquals(3, todos.size());
        assertTrue(todos.stream().anyMatch(todo -> "Learn Spring Boot".equals(todo.getTitle())));
        assertTrue(todos.stream().anyMatch(todo -> "Deploy to Kubernetes".equals(todo.getTitle())));
        assertTrue(todos.stream().anyMatch(todo -> "Spring Security".equals(todo.getTitle())));
    }

    @Test
    void findById_WhenTodoExists_ShouldReturnTodo() {
        // When
        Optional<Todo> todo = todoRepository.findById(testTodo1.getId());

        // Then
        assertTrue(todo.isPresent());
        assertEquals("Learn Spring Boot", todo.get().getTitle());
        assertEquals("Build a REST API with Spring Boot", todo.get().getDescription());
        assertFalse(todo.get().isCompleted());
    }

    @Test
    void findById_WhenTodoDoesNotExist_ShouldReturnEmpty() {
        // When
        Optional<Todo> todo = todoRepository.findById(999L);

        // Then
        assertFalse(todo.isPresent());
    }

    @Test
    void save_ShouldPersistNewTodo() {
        // Given
        Todo newTodo = new Todo();
        newTodo.setTitle("New Todo");
        newTodo.setDescription("New Description");
        newTodo.setCompleted(false);

        // When
        Todo savedTodo = todoRepository.save(newTodo);

        // Then
        assertNotNull(savedTodo.getId());
        assertEquals("New Todo", savedTodo.getTitle());
        assertEquals("New Description", savedTodo.getDescription());
        assertFalse(savedTodo.isCompleted());
        assertNotNull(savedTodo.getCreatedAt());
        assertNotNull(savedTodo.getUpdatedAt());

        // Verify it's actually persisted
        Optional<Todo> retrievedTodo = todoRepository.findById(savedTodo.getId());
        assertTrue(retrievedTodo.isPresent());
        assertEquals("New Todo", retrievedTodo.get().getTitle());
    }

    @Test
    void save_ShouldUpdateExistingTodo() {
        // Given
        testTodo1.setTitle("Updated Title");
        testTodo1.setDescription("Updated Description");
        testTodo1.setCompleted(true);

        // When
        Todo updatedTodo = todoRepository.save(testTodo1);

        // Then
        assertEquals(testTodo1.getId(), updatedTodo.getId());
        assertEquals("Updated Title", updatedTodo.getTitle());
        assertEquals("Updated Description", updatedTodo.getDescription());
        assertTrue(updatedTodo.isCompleted());

        // Verify it's actually updated in database
        Optional<Todo> retrievedTodo = todoRepository.findById(testTodo1.getId());
        assertTrue(retrievedTodo.isPresent());
        assertEquals("Updated Title", retrievedTodo.get().getTitle());
        assertEquals("Updated Description", retrievedTodo.get().getDescription());
        assertTrue(retrievedTodo.get().isCompleted());
    }

    @Test
    void deleteById_ShouldRemoveTodo() {
        // Given
        Long todoId = testTodo1.getId();

        // When
        todoRepository.deleteById(todoId);

        // Then
        Optional<Todo> deletedTodo = todoRepository.findById(todoId);
        assertFalse(deletedTodo.isPresent());
    }

    @Test
    void existsById_WhenTodoExists_ShouldReturnTrue() {
        // When
        boolean exists = todoRepository.existsById(testTodo1.getId());

        // Then
        assertTrue(exists);
    }

    @Test
    void existsById_WhenTodoDoesNotExist_ShouldReturnFalse() {
        // When
        boolean exists = todoRepository.existsById(999L);

        // Then
        assertFalse(exists);
    }

    @Test
    void findByCompleted_WhenCompletedTrue_ShouldReturnCompletedTodos() {
        // When
        List<Todo> completedTodos = todoRepository.findByCompleted(true);

        // Then
        assertNotNull(completedTodos);
        assertEquals(1, completedTodos.size());
        assertEquals("Deploy to Kubernetes", completedTodos.get(0).getTitle());
        assertTrue(completedTodos.get(0).isCompleted());
    }

    @Test
    void findByCompleted_WhenCompletedFalse_ShouldReturnPendingTodos() {
        // When
        List<Todo> pendingTodos = todoRepository.findByCompleted(false);

        // Then
        assertNotNull(pendingTodos);
        assertEquals(2, pendingTodos.size());
        assertTrue(pendingTodos.stream().allMatch(todo -> !todo.isCompleted()));
        assertTrue(pendingTodos.stream().anyMatch(todo -> "Learn Spring Boot".equals(todo.getTitle())));
        assertTrue(pendingTodos.stream().anyMatch(todo -> "Spring Security".equals(todo.getTitle())));
    }

    @Test
    void findByTitleContainingIgnoreCase_ShouldReturnMatchingTodos() {
        // When
        List<Todo> springTodos = todoRepository.findByTitleContainingIgnoreCase("spring");

        // Then
        assertNotNull(springTodos);
        assertEquals(2, springTodos.size());
        assertTrue(springTodos.stream().anyMatch(todo -> "Learn Spring Boot".equals(todo.getTitle())));
        assertTrue(springTodos.stream().anyMatch(todo -> "Spring Security".equals(todo.getTitle())));
    }

    @Test
    void findByTitleContainingIgnoreCase_WithCaseInsensitive_ShouldReturnMatchingTodos() {
        // When
        List<Todo> springTodos = todoRepository.findByTitleContainingIgnoreCase("SPRING");

        // Then
        assertNotNull(springTodos);
        assertEquals(2, springTodos.size());
        assertTrue(springTodos.stream().anyMatch(todo -> "Learn Spring Boot".equals(todo.getTitle())));
        assertTrue(springTodos.stream().anyMatch(todo -> "Spring Security".equals(todo.getTitle())));
    }

    @Test
    void findByTitleContainingIgnoreCase_WithNoMatches_ShouldReturnEmptyList() {
        // When
        List<Todo> noMatches = todoRepository.findByTitleContainingIgnoreCase("NonExistent");

        // Then
        assertNotNull(noMatches);
        assertTrue(noMatches.isEmpty());
    }

    @Test
    void findByCompletedTrue_ShouldReturnCompletedTodos() {
        // When
        List<Todo> completedTodos = todoRepository.findByCompletedTrue();

        // Then
        assertNotNull(completedTodos);
        assertEquals(1, completedTodos.size());
        assertEquals("Deploy to Kubernetes", completedTodos.get(0).getTitle());
        assertTrue(completedTodos.get(0).isCompleted());
    }

    @Test
    void findByCompletedFalse_ShouldReturnPendingTodos() {
        // When
        List<Todo> pendingTodos = todoRepository.findByCompletedFalse();

        // Then
        assertNotNull(pendingTodos);
        assertEquals(2, pendingTodos.size());
        assertTrue(pendingTodos.stream().allMatch(todo -> !todo.isCompleted()));
        assertTrue(pendingTodos.stream().anyMatch(todo -> "Learn Spring Boot".equals(todo.getTitle())));
        assertTrue(pendingTodos.stream().anyMatch(todo -> "Spring Security".equals(todo.getTitle())));
    }
}
