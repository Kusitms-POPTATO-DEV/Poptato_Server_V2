package server.poptato.user.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import server.poptato.auth.application.service.JwtService;
import server.poptato.user.application.service.UserService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    private String accessToken;
    private final Long userId = 1L;

    @BeforeEach
    void setup() {
        // Mock JwtService to return userId when verifying token
        accessToken = jwtService.createAccessToken(userId.toString());
    }

    @AfterEach
    void cleanup() {
        jwtService.deleteRefreshToken(userId.toString());
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void deleteUserSuccess() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.delete("/user")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andDo(print());

        verify(userService).deleteUser(userId);
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

        when(jwtService.verifyToken(anyString())).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.delete("/user")
                        .header("Authorization", "Bearer invalidToken"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }
}
