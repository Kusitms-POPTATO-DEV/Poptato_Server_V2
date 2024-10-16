package server.poptato.todo.api;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.web.servlet.MockMvc;
import server.poptato.auth.application.service.JwtService;
import server.poptato.todo.application.service.TodoService;
import server.poptato.todo.domain.repository.TodoRepository;
import server.poptato.user.application.service.UserService;
import server.poptato.user.infra.repository.JpaUserRepository;


import static org.mockito.Mockito.*;
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

        // todoService의 deleteTodoById 메서드가 호출될 때 예외가 발생하지 않도록 설정
        doNothing().when(todoService).deleteTodoById(todoId);

        mockMvc.perform(delete("/todo/{todoId}", todoId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        verify(todoService, times(1)).deleteTodoById(todoId);
    }

    @Test
    public void shouldReturnNotFound_WhenTodoDoesNotExist() throws Exception {
        Long todoId = 1L;

        // todoService의 deleteTodoById가 호출될 때 EntityNotFoundException 발생하도록 설정
        doThrow(new EntityNotFoundException("Todo not found with id: " + todoId))
                .when(todoService).deleteTodoById(todoId);

        mockMvc.perform(delete("/todo/{todoId}", todoId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());

        verify(todoService, times(1)).deleteTodoById(todoId);
    }
}
