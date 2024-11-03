package server.poptato.auth.api;

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
import server.poptato.auth.api.request.KakaoLoginRequestDto;
import server.poptato.auth.application.service.AuthService;
import server.poptato.auth.application.service.JwtService;
import server.poptato.global.dto.TokenPair;
import server.poptato.user.application.service.UserService;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AuthService authService;
    @MockBean
    private UserService userService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private TokenPair tokenPair;
    private Validator validator;
    private String accessToken;
    private String refreshToken;
    private final String userId = "1";

    @BeforeEach
    void createAccessToken_UserIdIsOne() {
        tokenPair = jwtService.generateTokenPair(userId);
        accessToken = tokenPair.accessToken();
        refreshToken = tokenPair.refreshToken();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterEach
    void deleteRefreshToken() {
        jwtService.deleteRefreshToken(userId);
    }

    @DisplayName("로그인 시, 액세스토큰이 비어있으면 Validator가 예외를 발생한다.")
    @Test
    public void login_ValidationException() {
        //given
        String emptyKakaoCode = " ";
        KakaoLoginRequestDto kakaoLoginRequestDto = KakaoLoginRequestDto.builder()
                .kakaoCode(emptyKakaoCode)
                .build();

        //when
        Set<ConstraintViolation<KakaoLoginRequestDto>> violations = validator.validate(kakaoLoginRequestDto);

        //then
        Assertions.assertEquals(violations.size(), 1);
    }

    @DisplayName("로그아웃 시, 성공한다.")
    @Test
    public void logout_Success() throws Exception {
        //when & then
        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @DisplayName("로그아웃 시, JWT 토큰이 없으면 예외가 발생한다.")
    @Test
    public void logout_UnAuthorizedException() throws Exception {
        //when & then
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @DisplayName("토큰 재발급 요청 시, 성공한다.")
    @Test
    public void refresh_Success() throws Exception {
        //when & then
        mockMvc.perform(post("/auth/refresh")
                        .content("{\"accessToken\": \"" + accessToken + "\", \"refreshToken\": \"" + refreshToken + "\"}")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }


}
