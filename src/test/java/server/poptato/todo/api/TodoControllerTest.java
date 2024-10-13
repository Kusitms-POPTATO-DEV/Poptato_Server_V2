package server.poptato.todo.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import server.poptato.todo.application.TodoService;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TodoControllerTest.class)
public class TodoControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private TodoService todoService;
    private String accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJBQ0NFU1NfVE9LRU4iLCJpYXQiOjE3MjgzN" +
            "jMwMzIsImV4cCI6MTcyODM2MzYzMiwiVVNFUl9JRCI6IjEifQ.Cz_tyPtuFMgJpLGZisSCH75pK-FjKIoNwmeR6_ERUDlWCPnx2Hqro" +
            "fu8on8QKYqg_zmJoBltdHlIBILiNElYfg";

    @DisplayName("투데이 목록 조회 시 성공적으로 응답된다.")
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

    @DisplayName("투데이 목록 조회 시 Query String에 Default 값이 적용된다.")
    @Test
    void 투데이_목록조회_쿼리스트링_기본값(){
        //when

    }

    @DisplayName("투데이 목록 조회 시 JWT로 사용자 아이디를 조회한다.")
    @Test
    void 투데이_목록조회_사용자_아이디(){
        //when

    }

    @DisplayName("투데이 목록 조회 시 헤더에 JWT가 없으면 예외가 발생한다.")
    @Test
    void 투데이_목록조회_JWT_예외(){
        //when

    }



}
