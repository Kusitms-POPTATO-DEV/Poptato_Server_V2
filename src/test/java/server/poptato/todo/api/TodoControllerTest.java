package server.poptato.todo.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import server.poptato.auth.application.service.JwtService;
import server.poptato.todo.application.TodoService;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TodoControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private TodoService todoService;
    @Autowired
    private JwtService jwtService;
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
}
