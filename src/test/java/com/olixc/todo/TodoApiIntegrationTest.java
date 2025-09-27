package com.olixc.todo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olixc.todo.entity.Todo;
import com.olixc.todo.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class TodoApiIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        todoRepository.deleteAll(); // Clean up before each test
    }

    @Test
    void fullCrudWorkflow_ShouldWorkEndToEnd() throws Exception {
        // 1. Create a new todo
        Todo newTodo = new Todo();
        newTodo.setTitle("Integration Test Todo");
        newTodo.setDescription("This is a test todo for integration testing");

        String createResponse = mockMvc.perform(post("/api/v1/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTodo)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Integration Test Todo"))
                .andExpect(jsonPath("$.description").value("This is a test todo for integration testing"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract the created todo ID
        Todo createdTodo = objectMapper.readValue(createResponse, Todo.class);
        Long todoId = createdTodo.getId();

        // 2. Verify the todo was saved to database
        assertTrue(todoRepository.existsById(todoId));
        Optional<Todo> savedTodo = todoRepository.findById(todoId);
        assertTrue(savedTodo.isPresent());
        assertEquals("Integration Test Todo", savedTodo.get().getTitle());

        // 3. Get all todos (should include our new todo)
        mockMvc.perform(get("/api/v1/todos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(todoId))
                .andExpect(jsonPath("$[0].title").value("Integration Test Todo"));

        // 4. Get the specific todo by ID
        mockMvc.perform(get("/api/v1/todos/" + todoId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(todoId))
                .andExpect(jsonPath("$.title").value("Integration Test Todo"))
                .andExpect(jsonPath("$.completed").value(false));

        // 5. Update the todo
        Todo updateData = new Todo();
        updateData.setTitle("Updated Integration Test Todo");
        updateData.setDescription("Updated description");
        updateData.setCompleted(true);

        mockMvc.perform(put("/api/v1/todos/" + todoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(todoId))
                .andExpect(jsonPath("$.title").value("Updated Integration Test Todo"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.completed").value(true));

        // 6. Verify the update in database
        Optional<Todo> updatedTodo = todoRepository.findById(todoId);
        assertTrue(updatedTodo.isPresent());
        assertEquals("Updated Integration Test Todo", updatedTodo.get().getTitle());
        assertEquals("Updated description", updatedTodo.get().getDescription());
        assertTrue(updatedTodo.get().isCompleted());

        // 7. Test filtering - get completed todos
        mockMvc.perform(get("/api/v1/todos/completed"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(todoId))
                .andExpect(jsonPath("$[0].completed").value(true));

        // 8. Test filtering - get pending todos (should be empty)
        mockMvc.perform(get("/api/v1/todos/pending"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));

        // 9. Test search functionality
        mockMvc.perform(get("/api/v1/todos/search")
                        .param("title", "Updated"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Updated Integration Test Todo"));

        // 10. Delete the todo
        mockMvc.perform(delete("/api/v1/todos/" + todoId))
                .andExpect(status().isNoContent());

        // 11. Verify the todo was deleted from database
        assertFalse(todoRepository.existsById(todoId));

        // 12. Try to get the deleted todo (should return 404)
        mockMvc.perform(get("/api/v1/todos/" + todoId))
                .andExpect(status().isNotFound());

        // 13. Verify no todos remain
        mockMvc.perform(get("/api/v1/todos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createMultipleTodos_AndTestFiltering() throws Exception {
        // Create multiple todos with different completion statuses
        Todo todo1 = new Todo();
        todo1.setTitle("Learn Spring Boot");
        todo1.setDescription("Master Spring Boot framework");
        todo1.setCompleted(false);

        Todo todo2 = new Todo();
        todo2.setTitle("Deploy to Kubernetes");
        todo2.setDescription("Deploy application to K8s cluster");
        todo2.setCompleted(true);

        Todo todo3 = new Todo();
        todo3.setTitle("Spring Security");
        todo3.setDescription("Add authentication and authorization");
        todo3.setCompleted(false);

        // Create todos via API
        mockMvc.perform(post("/api/v1/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todo1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todo2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todo3)))
                .andExpect(status().isCreated());

        // Verify all todos were created
        List<Todo> allTodos = todoRepository.findAll();
        assertEquals(3, allTodos.size());

        // Test completed todos filter
        mockMvc.perform(get("/api/v1/todos/completed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Deploy to Kubernetes"));

        // Test pending todos filter
        mockMvc.perform(get("/api/v1/todos/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)));

        // Test search functionality
        mockMvc.perform(get("/api/v1/todos/search")
                        .param("title", "Spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title", hasItems("Learn Spring Boot", "Spring Security")));
    }

    @Test
    void handleInvalidRequests_ShouldReturnAppropriateErrors() throws Exception {
        // Test getting non-existent todo
        mockMvc.perform(get("/api/v1/todos/999"))
                .andExpect(status().isNotFound());

        // Test updating non-existent todo
        Todo updateData = new Todo();
        updateData.setTitle("Updated Title");

        mockMvc.perform(put("/api/v1/todos/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isNotFound());

        // Test deleting non-existent todo
        mockMvc.perform(delete("/api/v1/todos/999"))
                .andExpect(status().isNotFound());

        // Test invalid JSON
        mockMvc.perform(post("/api/v1/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andExpect(status().isBadRequest());
    }
}
