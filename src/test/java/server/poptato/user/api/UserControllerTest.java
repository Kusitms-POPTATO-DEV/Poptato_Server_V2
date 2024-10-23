package server.poptato.user.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
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
import server.poptato.user.application.service.UserService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

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

    @Test
    @DisplayName("프로필 조회 성공 테스트")
    void getUserNameAndEmail() throws Exception {
        mockMvc.perform(get("/user/mypage")
                        .header("Authorization", "Bearer "+accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    @DisplayName("프로필 조회 실패 테스트 - invalid token")
    void getUserNameAndEmailFailedInvalidToken() throws Exception {
        String invalidToken = "invalidToken";
        mockMvc.perform(get("/user/mypage")
                        .header("Authorization", "Bearer "+invalidToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("회원 탈퇴 성공 - 토큰 검증 후 응답 확인")
    void deleteUserSuccess() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.delete("/user")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 토큰 없음")
    void deleteUserFailureNoToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/user"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 유효하지 않은 토큰")
    void deleteUserFailureInvalidToken() throws Exception {
        String invalidToken = "invalidToken";

        mockMvc.perform(MockMvcRequestBuilders.delete("/user")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }
}
