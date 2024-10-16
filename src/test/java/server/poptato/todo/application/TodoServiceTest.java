package server.poptato.todo.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import server.poptato.user.exception.UserException;
import server.poptato.user.exception.errorcode.UserExceptionErrorCode;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class TodoServiceTest {
    @Autowired
    private TodoService todoService;
    @DisplayName("존재하지 않는 유저일 경우 예외가 발생한다.")
    @Test
    void 해당하는_유저정보_없는_예외(){
        //given
        Long notExistUserId = 100L;
        int page = 0;
        int size = 8;
        //when & then
        assertThatThrownBy(()-> todoService.getTodayList(notExistUserId,page,size))
                .isInstanceOf(UserException.class)
                .hasMessage(UserExceptionErrorCode.USER_NOT_EXIST.getMessage());
    }
    @DisplayName("size=8을 요청하면, 투데이 목록 조회 시 8개의 데이터만 응답된다.")
    @Test
    void 데이터_8개만_응답(){
        //given
        Long userId = 1L;
        int page = 0;
        int size = 8;
        //when & then
        assertThat(todoService.getTodayList(userId,page,size).getTodays().size(),size);
    }
}