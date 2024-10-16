package server.poptato.todo.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import server.poptato.auth.application.service.JwtService;
import server.poptato.todo.application.TodoService;
import server.poptato.todo.exception.TodoException;
import server.poptato.user.application.service.UserService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static server.poptato.todo.exception.errorcode.TodoExceptionErrorCode.TODO_NOT_EXIST;

@SpringBootTest
@AutoConfigureMockMvc
public class TodoControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private TodoService todoService;
    @MockBean
    private UserService userService;
    @Autowired
    private JwtService jwtService;
    @MockBean
    private RedisTemplate<String, String> redisTemplate;
    private String accessToken;
    private final String userId = "1";

    @BeforeEach
    void userId가_1인_액세스토큰_생성() {
        accessToken = jwtService.createAccessToken(userId);
    }

    @AfterEach
    void 액세스토큰_비활성화() {
        jwtService.deleteRefreshToken(userId);
    }
    @DisplayName("투데이 목록 조회 시 page와 size를 query string으로 받고 헤더에 accessToken을 담아 요청한다.")
    @Test
    void 투데이_목록조회_성공응답() throws Exception {
        //when
        mockMvc.perform(MockMvcRequestBuilders.get("/todays")
                        .param("page","0")
                        .param("size","8")
                        .header("Authorization", "Bearer "+accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @DisplayName("투데이 목록 조회 시 Query String에 Default 값이 적용되고, JWT로 사용자 아이디를 조회한다.")
    @Test
    void 투데이_목록조회_쿼리스트링_기본값() throws Exception {
        //when
        mockMvc.perform(MockMvcRequestBuilders.get("/todays")
                        .header("Authorization", "Bearer "+accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        verify(todoService).getTodayList(1,0, 8);
    }

    @DisplayName("투데이 목록 조회 시 헤더에 JWT가 없으면 예외가 발생한다.")
    @Test
    void 투데이_목록조회_JWT_예외() throws Exception {
        //when
        mockMvc.perform(MockMvcRequestBuilders.get("/todays")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }
    @Test
    public void shouldReturnNoContent_WhenTodoIsDeleted() throws Exception { //투두 있을 때
        Long todoId = 1L;

        // todoService의 deleteTodoById 메서드가 호출될 때 예외가 발생하지 않도록 설정
        doNothing().when(todoService).deleteTodoById(todoId);

        mockMvc.perform(delete("/todo/{todoId}", todoId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        verify(todoService, times(1)).deleteTodoById(todoId);
    }

    @Test
    public void shouldReturnNotFound_WhenTodoDoesNotExist() throws Exception { //투두 없을 때
        Long todoId = 1L;

        // todoService의 deleteTodoById가 호출될 때 exception  발생하도록 설정
        doThrow(new TodoException(TODO_NOT_EXIST))
                .when(todoService).deleteTodoById(todoId);

        mockMvc.perform(delete("/todo/{todoId}", todoId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest());

        verify(todoService, times(1)).deleteTodoById(todoId);
    }
}
