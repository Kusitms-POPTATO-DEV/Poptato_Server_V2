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
import server.poptato.todo.infra.repository.JpaTodoRepository;
import server.poptato.user.application.service.UserService;
import server.poptato.user.infra.repository.JpaUserRepository;

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
    @DisplayName("회원 탈퇴 성공 - 토큰 검증 후 응답 확인")
    void deleteUserSuccess() throws Exception {
        // 토큰 검증 성공
        when(jwtService.verifyToken(anyString())).thenReturn(true);
        when(jwtService.getUserIdInToken(anyString())).thenReturn(userId.toString());

        // UserService의 deleteUser()가 호출되는지 확인
        doNothing().when(userService).deleteUser(userId);

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
}
