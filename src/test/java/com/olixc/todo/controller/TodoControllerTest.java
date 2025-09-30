package com.olixc.todo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olixc.todo.entity.Todo;
import com.olixc.todo.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TodoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TodoService todoService;

    @InjectMocks
    private TodoController todoController;

    private ObjectMapper objectMapper;

    private Todo testTodo;
    private List<Todo> testTodos;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(todoController).build();
        
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
    void getAllTodos_ShouldReturnAllTodos() throws Exception {
        // Given
        when(todoService.getAllTodos()).thenReturn(testTodos);

        // When & Then
        mockMvc.perform(get("/api/v1/todos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Todo"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Completed Todo"));

        verify(todoService, times(1)).getAllTodos();
    }

    @Test
    void getTodoById_WhenTodoExists_ShouldReturnTodo() throws Exception {
        // Given
        when(todoService.getTodoById(1L)).thenReturn(Optional.of(testTodo));

        // When & Then
        mockMvc.perform(get("/api/v1/todos/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Todo"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.completed").value(false));

        verify(todoService, times(1)).getTodoById(1L);
    }

    @Test
    void getTodoById_WhenTodoDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(todoService.getTodoById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/todos/999"))
                .andExpect(status().isNotFound());

        verify(todoService, times(1)).getTodoById(999L);
    }

    @Test
    void createTodo_ShouldCreateAndReturnTodo() throws Exception {
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

        when(todoService.createTodo(any(Todo.class))).thenReturn(savedTodo);

        // When & Then
        mockMvc.perform(post("/api/v1/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTodo)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.title").value("New Todo"))
                .andExpect(jsonPath("$.description").value("New Description"))
                .andExpect(jsonPath("$.completed").value(false));

        verify(todoService, times(1)).createTodo(any(Todo.class));
    }

    @Test
    void updateTodo_WhenTodoExists_ShouldUpdateAndReturnTodo() throws Exception {
        // Given
        Todo updateData = new Todo();
        updateData.setTitle("Updated Todo");
        updateData.setDescription("Updated Description");
        updateData.setCompleted(true);

        Todo updatedTodo = new Todo();
        updatedTodo.setId(1L);
        updatedTodo.setTitle("Updated Todo");
        updatedTodo.setDescription("Updated Description");
        updatedTodo.setCompleted(true);
        updatedTodo.setCreatedAt(LocalDateTime.now());
        updatedTodo.setUpdatedAt(LocalDateTime.now());

        when(todoService.updateTodo(eq(1L), any(Todo.class))).thenReturn(updatedTodo);

        // When & Then
        mockMvc.perform(put("/api/v1/todos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Todo"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.completed").value(true));

        verify(todoService, times(1)).updateTodo(eq(1L), any(Todo.class));
    }

    @Test
    void updateTodo_WhenTodoDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        Todo updateData = new Todo();
        updateData.setTitle("Updated Todo");

        when(todoService.updateTodo(eq(999L), any(Todo.class))).thenReturn(null);

        // When & Then
        mockMvc.perform(put("/api/v1/todos/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isNotFound());

        verify(todoService, times(1)).updateTodo(eq(999L), any(Todo.class));
    }

    @Test
    void deleteTodo_WhenTodoExists_ShouldDeleteAndReturnNoContent() throws Exception {
        // Given
        when(todoService.deleteTodo(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/v1/todos/1"))
                .andExpect(status().isNoContent());

        verify(todoService, times(1)).deleteTodo(1L);
    }

    @Test
    void deleteTodo_WhenTodoDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(todoService.deleteTodo(999L)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/v1/todos/999"))
                .andExpect(status().isNotFound());

        verify(todoService, times(1)).deleteTodo(999L);
    }

    @Test
    void getCompletedTodos_ShouldReturnCompletedTodos() throws Exception {
        // Given
        List<Todo> completedTodos = Arrays.asList(testTodos.get(1)); // Only the completed one
        when(todoService.getCompletedTodos()).thenReturn(completedTodos);

        // When & Then
        mockMvc.perform(get("/api/v1/todos/completed"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].completed").value(true));

        verify(todoService, times(1)).getCompletedTodos();
    }

    @Test
    void getPendingTodos_ShouldReturnPendingTodos() throws Exception {
        // Given
        List<Todo> pendingTodos = Arrays.asList(testTodos.get(0)); // Only the pending one
        when(todoService.getPendingTodos()).thenReturn(pendingTodos);

        // When & Then
        mockMvc.perform(get("/api/v1/todos/pending"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].completed").value(false));

        verify(todoService, times(1)).getPendingTodos();
    }

    @Test
    void searchTodosByTitle_ShouldReturnMatchingTodos() throws Exception {
        // Given
        List<Todo> searchResults = Arrays.asList(testTodo);
        when(todoService.searchTodosByTitle("Test")).thenReturn(searchResults);

        // When & Then
        mockMvc.perform(get("/api/v1/todos/search")
                        .param("title", "Test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Todo"));

        verify(todoService, times(1)).searchTodosByTitle("Test");
    }
}
