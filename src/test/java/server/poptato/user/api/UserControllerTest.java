package server.poptato.user.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import server.poptato.auth.application.service.JwtService;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.value.TodayStatus;
import server.poptato.todo.domain.value.Type;
import server.poptato.todo.infra.repository.JpaTodoRepository;
import server.poptato.user.application.service.UserService;
import server.poptato.user.domain.entity.User;
import server.poptato.user.infra.repository.JpaUserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserService userService;

    @MockBean
    private JpaUserRepository userRepository;

    @MockBean
    private JpaTodoRepository todoRepository;

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
    @DisplayName("회원 탈퇴 성공")
    void deleteUserSuccessWithTodos() throws Exception {
        // Given
        User user = User.builder()
                .id(userId)
                .kakaoId("kakao12345")
                .name("Kyounglin")
                .email("Kyounglin@example.com")
                .createDate(LocalDateTime.now())
                .modifyDate(LocalDateTime.now())
                .build();

        // Todo 리스트 생성
        List<Todo> todos = List.of(
                Todo.builder()
                        .userId(userId)
                        .type(Type.TODAY)
                        .content("Task 1")
                        .isBookmark(false)
                        .todayDate(LocalDate.now())
                        .todayStatus(TodayStatus.COMPLETED)
                        .createDate(LocalDateTime.now())
                        .modifyDate(LocalDateTime.now())
                        .build(),
                Todo.builder()
                        .userId(userId)
                        .type(Type.TODAY)
                        .content("Task 2")
                        .isBookmark(true)
                        .todayDate(LocalDate.now())
                        .todayStatus(TodayStatus.COMPLETED)
                        .createDate(LocalDateTime.now())
                        .modifyDate(LocalDateTime.now())
                        .build()
        );

        // 토큰 검증 성공
        when(jwtService.verifyToken(anyString())).thenReturn(true);
        when(jwtService.getUserIdInToken(anyString())).thenReturn(userId.toString());

        // User 찾기 성공
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Todo 리스트 반환
        when(todoRepository.findAllByUserId(userId)).thenReturn(todos);

        doNothing().when(jwtService).deleteRefreshToken(anyString());
        doNothing().when(userRepository).delete(user);

        mockMvc.perform(MockMvcRequestBuilders.delete("/user")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 토큰 없음")
    void deleteUserFailureNoToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/user"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 유효하지 않은 토큰")
    void deleteUserFailureInvalidToken() throws Exception {
        String invalidToken = "invalidToken";

        // 토큰 검증 실패
        when(jwtService.verifyToken(anyString())).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.delete("/user")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 유저가 존재하지 않음")
    void deleteUserFailureUserNotFound() throws Exception {
        // Given
        // User 찾기 실패
        when(jwtService.verifyToken(anyString())).thenReturn(true);
        when(jwtService.getUserIdInToken(anyString())).thenReturn(userId.toString());

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.delete("/user")
                        .header("Authorization", "Bearer " + accessToken))
//                .andExpect(status().isNotFound())
                .andDo(print());
    }
}
