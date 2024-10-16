package server.poptato.todo.api;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.web.servlet.MockMvc;
import server.poptato.auth.application.service.JwtService;
import server.poptato.todo.domain.repository.TodoRepository;
import server.poptato.user.application.service.UserService;
import server.poptato.user.infra.repository.JpaUserRepository;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TodoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;
    @MockBean
    private TodoService todoService;

    @MockBean
    private UserService userService;

    @MockBean
    private JpaUserRepository userRepository;

    @MockBean
    private TodoRepository todoRepository;

    @MockBean
    private RedisTemplate<String, String> redisTemplate;

    private String accessToken;
    private final Long userId = 1L;

    @BeforeEach
    void createAccessToken() {
        accessToken = jwtService.createAccessToken(userId.toString());
    }

    @AfterEach
    void deleteRefreshToken() {
        jwtService.deleteRefreshToken(userId.toString());
    }

    @Test
    public void shouldReturnNoContent_WhenTodoIsDeleted() throws Exception {
        Long todoId = 1L;

        mockMvc.perform(delete("/todo/{todoId}", todoId))
                .andExpect(status().isNoContent());
    }

    @Test
    public void shouldReturnNotFound_WhenTodoDoesNotExist() throws Exception {
        Long todoId = 1L;
        doThrow(new EntityNotFoundException("Todo not found with id: " + todoId))
                .when(todoService).deleteTodoById(todoId);

        mockMvc.perform(delete("/todo/{todoId}", todoId))
                .andExpect(status().isNotFound());
    }
}
