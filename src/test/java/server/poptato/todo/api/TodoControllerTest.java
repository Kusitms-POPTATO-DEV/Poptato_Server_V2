package server.poptato.todo.api;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import server.poptato.auth.application.service.JwtService;
import server.poptato.todo.api.request.BacklogCreateRequestDto;
import server.poptato.todo.api.request.DragAndDropRequestDto;
import server.poptato.todo.api.request.SwipeRequestDto;
import server.poptato.todo.application.TodoBacklogService;
import server.poptato.todo.application.TodoService;
import server.poptato.todo.application.TodoTodayService;
import server.poptato.todo.exception.TodoException;
import server.poptato.user.application.service.UserService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Set;

import static org.mockito.Mockito.verify;
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
    private TodoBacklogService todoBacklogService;
    @MockBean
    private TodoTodayService todoTodayService;
    @MockBean
    private UserService userService;
    @Autowired
    private JwtService jwtService;
    @MockBean
    private RedisTemplate<String, String> redisTemplate;
    private Validator validator;
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
        mockMvc.perform(get("/todays")
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
        mockMvc.perform(get("/todays")
                        .header("Authorization", "Bearer "+accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
        LocalDate todayDate = LocalDate.now();

        verify(todoTodayService).getTodayList(1,0, 8,todayDate);
    }

    @DisplayName("투데이 목록 조회 시 헤더에 JWT가 없으면 예외가 발생한다.")
    @Test
    void 투데이_목록조회_JWT_예외() throws Exception {
        //when
        mockMvc.perform(get("/todays")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }
    @Test
    public void shouldReturnNoContent_WhenTodoIsDeleted() throws Exception { //투두 있을 때
        Long todoId = 1L;
        Long userId = 1L;

        // todoService의 deleteTodoById 메서드가 호출될 때 예외가 발생하지 않도록 설정
        doNothing().when(todoService).deleteTodoById(userId, todoId);

        mockMvc.perform(delete("/todo/{todoId}", todoId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        verify(todoService, times(1)).deleteTodoById(userId, todoId);
    }

    @Test
    public void shouldReturnNotFound_WhenTodoDoesNotExist() throws Exception { //투두 없을 때
        Long todoId = 144L;
        Long userId = 1L;

        // todoService의 deleteTodoById가 호출될 때 exception  발생하도록 설정
        doThrow(new TodoException(TODO_NOT_EXIST))
                .when(todoService).deleteTodoById(userId, todoId);

        mockMvc.perform(delete("/todo/{todoId}", todoId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest());

        verify(todoService, times(1)).deleteTodoById(userId, todoId);
    }

    @DisplayName("백로그 목록 조회 시 page와 size를 query string으로 받고 헤더에 accessToken을 담아 요청한다.")
    @Test
    void 백로그_목록조회_성공응답() throws Exception {
        //when
        mockMvc.perform(get("/backlogs")
                        .param("page","0")
                        .param("size","8")
                        .header("Authorization", "Bearer "+accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @DisplayName("백로그 목록 조회 시 Query String에 Default 값이 적용되고, JWT로 사용자 아이디를 조회한다.")
    @Test
    void 백로그_목록조회_쿼리스트링_기본값() throws Exception {
        //when
        mockMvc.perform(get("/backlogs")
                        .header("Authorization", "Bearer "+accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        verify(todoBacklogService).getBacklogList(1L,0, 8);
    }

    @DisplayName("백로그 목록 조회 시 헤더에 JWT가 없으면 예외가 발생한다.")
    @Test
    void 백로그_목록조회_JWT_예외() throws Exception {
        //when
        mockMvc.perform(get("/backlogs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("isBookmark 토글 시 응답이 정상적으로 반환되는지 확인")
    public void shouldReturnOk_WhenIsBookmarkToggled() throws Exception {
        Long todoId = 1L;
        Long userId = 1L;

        // todoService의 toggleIsBookmark 메서드가 호출될 때 예외가 발생하지 않도록 설정
        doNothing().when(todoService).toggleIsBookmark(userId, todoId);

        mockMvc.perform(patch("/todo/{todoId}/bookmark", todoId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // todoService의 toggleIsBookmark 메서드가 한 번 호출되었는지 확인
        verify(todoService, times(1)).toggleIsBookmark(userId, todoId);
    }

    @DisplayName("히스토리 목록 조회 시 page와 size를 query string으로 받고 헤더에 accessToken을 담아 요청한다.")
    @Test
    void 히스토리_목록조회_성공응답() throws Exception {
        // when
        mockMvc.perform(get("/histories")
                        .param("page", "0")
                        .param("size", "15")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @DisplayName("스와이프 시 요청 바디에 todoId가 없으면 Validator가 잡는다.")
    @Test
    void 스와이프_요청바디_예외(){
        //given
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        SwipeRequestDto request = SwipeRequestDto.builder()
                        .todoId(null).build();

        //when
        Set<ConstraintViolation<SwipeRequestDto>> violations = validator.validate(request);
        //then
        Assertions.assertEquals(violations.size(), 1);
    }

    @DisplayName("스와이프 요청 시 성공한다.")
    @Test
    void 스와이프_성공_응답() throws Exception {
        //when
        mockMvc.perform(patch("/swipe")
                        .content("{\"todoId\": 1}")
                        .header("Authorization", "Bearer "+accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @DisplayName("히스토리 목록 조회 시 Query String에 기본값이 적용되고, JWT로 사용자 아이디를 조회한다.")
    @Test
    void 히스토리_목록조회_쿼리스트링_기본값() throws Exception {
        // when
        mockMvc.perform(get("/histories")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        // 기본값이 적용된 서비스 메서드 호출 확인 (userId = 1L, page = 0, size = 15)
        verify(todoBacklogService).getHistories(1L, 0, 15);
    }

    @DisplayName("히스토리 목록 조회 시 헤더에 JWT가 없으면 예외가 발생한다.")
    @Test
    void 히스토리_목록조회_JWT_예외() throws Exception {
        // when
        mockMvc.perform(get("/histories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())  // JWT가 없으므로 401 응답
                .andDo(print());
    }
    @DisplayName("드래그앤드롭 시 요청 바디에 type이나 list가 없으면 Validator가 잡는다.")
    @Test
    void 드래그앤드롭_요청바디_예외(){
        //given
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        DragAndDropRequestDto request = DragAndDropRequestDto.builder()
                .type(null)
                .todoIds(new ArrayList<>())
                .build();

        //when
        Set<ConstraintViolation<DragAndDropRequestDto>> violations = validator.validate(request);
        //then
        Assertions.assertEquals(violations.size(), 2);
    }

    @DisplayName("드래그앤드롭 요청 시 성공한다.")
    @Test
    void 드래그앤드롭_성공_응답() throws Exception {
        //when
        mockMvc.perform(patch("/dragAndDrop")
                        .content("{\"type\": \"TODAY\", \"todoIds\": [1, 2, 3, 4]}")
                        .header("Authorization", "Bearer "+accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @DisplayName("백로그 생성 요청 바디에 content가 없거나 비어있으면 Validator가 잡는다.")
    @Test
    void 백로그_생성_요청바디_예외(){
        //given
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        BacklogCreateRequestDto request = BacklogCreateRequestDto.builder()
                .content(" ")
                .build();

        //when
        Set<ConstraintViolation<BacklogCreateRequestDto>> violations = validator.validate(request);
        //then
        Assertions.assertEquals(violations.size(), 1);
    }

    @DisplayName("백로그 생성 요청 시 성공한다.")
    @Test
    void 백로그_생성_성공_응답() throws Exception {
        //when
        mockMvc.perform(post("/backlog")
                        .content("{\"content\": \"할일 내용 수정본\"}")
                        .header("Authorization", "Bearer "+accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @DisplayName("할 일 정보 요청 시 성공한다.")
    @Test
    void 할일_정보_요청_성공_응답() throws Exception {
        //given
        Long todoId = 1L;

        //when
        mockMvc.perform(get("/todo/{todoId}", todoId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @DisplayName("어제 한 일 조회 시 Query String에 기본값이 적용되고, JWT로 사용자 아이디를 조회한다.")
    @Test
    void 어제한일_쿼리스트링_기본값() throws Exception {
        // when
        mockMvc.perform(get("/yesterdays")
                        .header("Authorization", "Bearer "+accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @DisplayName("어제 한 일 조회 시 헤더에 JWT가 없으면 예외가 발생한다.")
    @Test
    void 어제한일_JWT_예외() throws Exception {
        // when
        mockMvc.perform(get("/yesterdays")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())  // JWT가 없으므로 401 응답
                .andDo(print());

    }

    @DisplayName("마감기한 수정 요청 시 성공한다.")
    @Test
    void 마감기한_수정_요청_성공_응답() throws Exception {
        //given
        Long todoId = 1L;

        //when
        mockMvc.perform(patch("/todo/{todoId}/deadline", todoId)
                        .header("Authorization", "Bearer " + accessToken)
                        .content("{\"deadline\": \"2024-12-25\"}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @DisplayName("할 일 내용 수정 요청 시 성공한다.")
    @Test
    void 할일_내용_수정_요청_성공_응답() throws Exception {
        //given
        Long todoId = 1L;

        //when
        mockMvc.perform(patch("/todo/{todoId}/content", todoId)
                        .header("Authorization", "Bearer " + accessToken)
                        .content("{\"content\": \"내용 수정\"}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @DisplayName("투데이 달성여부 변경 요청 시 성공한다.")
    @Test
    void 투데이_달성여부_변경_요청_성공_응답() throws Exception {
        //given
        Long todoId = 1L;

        //when
        mockMvc.perform(patch("/todo/{todoId}/achieve", todoId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }
}
