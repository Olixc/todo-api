package com.olixc.todo.service;

import com.olixc.todo.entity.Todo;
import com.olixc.todo.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private TodoService todoService;

    private Todo testTodo;
    private List<Todo> testTodos;

    @BeforeEach
    void setUp() {
        testTodo = new Todo();
        testTodo.setId(1L);
        testTodo.setTitle("Test Todo");
        testTodo.setDescription("Test Description");
        testTodo.setCompleted(false);
        testTodo.setCreatedAt(LocalDateTime.now());
        testTodo.setUpdatedAt(LocalDateTime.now());

        Todo completedTodo = new Todo();
        completedTodo.setId(2L);
        completedTodo.setTitle("Completed Todo");
        completedTodo.setDescription("Completed Description");
        completedTodo.setCompleted(true);
        completedTodo.setCreatedAt(LocalDateTime.now());
        completedTodo.setUpdatedAt(LocalDateTime.now());

        testTodos = Arrays.asList(testTodo, completedTodo);
    }

    @Test
    void getAllTodos_ShouldReturnAllTodos() {
        // Given
        when(todoRepository.findAll()).thenReturn(testTodos);

        // When
        List<Todo> result = todoService.getAllTodos();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Todo", result.get(0).getTitle());
        assertEquals("Completed Todo", result.get(1).getTitle());
        verify(todoRepository, times(1)).findAll();
    }

    @Test
    void getTodoById_WhenTodoExists_ShouldReturnTodo() {
        // Given
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));

        // When
        Optional<Todo> result = todoService.getTodoById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Test Todo", result.get().getTitle());
        assertEquals("Test Description", result.get().getDescription());
        assertEquals(false, result.get().isCompleted());
        verify(todoRepository, times(1)).findById(1L);
    }

    @Test
    void getTodoById_WhenTodoDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(todoRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Todo> result = todoService.getTodoById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(todoRepository, times(1)).findById(999L);
    }

    @Test
    void createTodo_ShouldSaveAndReturnTodo() {
        // Given
        Todo newTodo = new Todo();
        newTodo.setTitle("New Todo");
        newTodo.setDescription("New Description");

        Todo savedTodo = new Todo();
        savedTodo.setId(3L);
        savedTodo.setTitle("New Todo");
        savedTodo.setDescription("New Description");
        savedTodo.setCompleted(false);
        savedTodo.setCreatedAt(LocalDateTime.now());
        savedTodo.setUpdatedAt(LocalDateTime.now());

        when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);

        // When
        Todo result = todoService.createTodo(newTodo);

        // Then
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("New Todo", result.getTitle());
        assertEquals("New Description", result.getDescription());
        assertEquals(false, result.isCompleted());
        verify(todoRepository, times(1)).save(newTodo);
    }

    @Test
    void updateTodo_WhenTodoExists_ShouldUpdateAndReturnTodo() {
        // Given
        Todo existingTodo = new Todo();
        existingTodo.setId(1L);
        existingTodo.setTitle("Original Title");
        existingTodo.setDescription("Original Description");
        existingTodo.setCompleted(false);

        Todo updateData = new Todo();
        updateData.setTitle("Updated Title");
        updateData.setDescription("Updated Description");
        updateData.setCompleted(true);

        when(todoRepository.findById(1L)).thenReturn(Optional.of(existingTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(existingTodo);

        // When
        Todo result = todoService.updateTodo(1L, updateData);

        // Then
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(true, result.isCompleted());
        verify(todoRepository, times(1)).findById(1L);
        verify(todoRepository, times(1)).save(existingTodo);
    }

    @Test
    void updateTodo_WhenTodoDoesNotExist_ShouldReturnNull() {
        // Given
        Todo updateData = new Todo();
        updateData.setTitle("Updated Title");

        when(todoRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Todo result = todoService.updateTodo(999L, updateData);

        // Then
        assertNull(result);
        verify(todoRepository, times(1)).findById(999L);
        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    void deleteTodo_WhenTodoExists_ShouldDeleteAndReturnTrue() {
        // Given
        when(todoRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = todoService.deleteTodo(1L);

        // Then
        assertTrue(result);
        verify(todoRepository, times(1)).existsById(1L);
        verify(todoRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteTodo_WhenTodoDoesNotExist_ShouldReturnFalse() {
        // Given
        when(todoRepository.existsById(999L)).thenReturn(false);

        // When
        boolean result = todoService.deleteTodo(999L);

        // Then
        assertFalse(result);
        verify(todoRepository, times(1)).existsById(999L);
        verify(todoRepository, never()).deleteById(anyLong());
    }

    @Test
    void getTodosByStatus_WhenCompleted_ShouldReturnCompletedTodos() {
        // Given
        List<Todo> completedTodos = Arrays.asList(testTodos.get(1)); // Only completed todo
        when(todoRepository.findByCompleted(true)).thenReturn(completedTodos);

        // When
        List<Todo> result = todoService.getTodosByStatus(true);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isCompleted());
        verify(todoRepository, times(1)).findByCompleted(true);
    }

    @Test
    void getTodosByStatus_WhenPending_ShouldReturnPendingTodos() {
        // Given
        List<Todo> pendingTodos = Arrays.asList(testTodos.get(0)); // Only pending todo
        when(todoRepository.findByCompleted(false)).thenReturn(pendingTodos);

        // When
        List<Todo> result = todoService.getTodosByStatus(false);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).isCompleted());
        verify(todoRepository, times(1)).findByCompleted(false);
    }

    @Test
    void searchTodosByTitle_ShouldReturnMatchingTodos() {
        // Given
        List<Todo> searchResults = Arrays.asList(testTodo);
        when(todoRepository.findByTitleContainingIgnoreCase("Test")).thenReturn(searchResults);

        // When
        List<Todo> result = todoService.searchTodosByTitle("Test");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Todo", result.get(0).getTitle());
        verify(todoRepository, times(1)).findByTitleContainingIgnoreCase("Test");
    }

    @Test
    void getCompletedTodos_ShouldReturnCompletedTodos() {
        // Given
        List<Todo> completedTodos = Arrays.asList(testTodos.get(1));
        when(todoRepository.findByCompletedTrue()).thenReturn(completedTodos);

        // When
        List<Todo> result = todoService.getCompletedTodos();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isCompleted());
        verify(todoRepository, times(1)).findByCompletedTrue();
    }

    @Test
    void getPendingTodos_ShouldReturnPendingTodos() {
        // Given
        List<Todo> pendingTodos = Arrays.asList(testTodos.get(0));
        when(todoRepository.findByCompletedFalse()).thenReturn(pendingTodos);

        // When
        List<Todo> result = todoService.getPendingTodos();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).isCompleted());
        verify(todoRepository, times(1)).findByCompletedFalse();
    }
}
