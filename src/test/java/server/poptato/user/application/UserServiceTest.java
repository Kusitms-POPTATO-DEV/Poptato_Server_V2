package server.poptato.user.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import server.poptato.auth.application.response.LoginResponseDto;
import server.poptato.deleteReason.domain.entity.DeleteReason;
import server.poptato.deleteReason.domain.repository.DeleteReasonRepository;
import server.poptato.deleteReason.domain.value.Reason;
import server.poptato.user.api.request.UserDeleteRequestDTO;
import server.poptato.user.application.response.UserInfoResponseDto;
import server.poptato.user.application.service.UserService;
import server.poptato.user.domain.repository.UserRepository;
import server.poptato.user.exception.UserException;
import server.poptato.user.exception.errorcode.UserExceptionErrorCode;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeleteReasonRepository deleteReasonRepository;

    @Test
    @DisplayName("마이페이지 조회 시 성공한다.")
    public void getUserInfo_Success() {
        // given
        Long userId = 1L;

        // when
        UserInfoResponseDto responseDto = userService.getUserInfo(userId);

        // then
        assertThat(responseDto.name()).isEqualTo("Poptato");
        assertThat(responseDto.email()).isEqualTo("poptato@naver.com");
        assertThat(responseDto.imageUrl()).isEqualTo("https://image");
    }

    @Test
    @DisplayName("마아페이지 조회 시, 유효하지 않은 userId가 주어졌을 때 UserException을 던진다")
    public void getUserInfo_UserNotExistException() {
        // given
        Long invalidUserId = 2L;

        // when & then
        assertThatThrownBy(()-> userService.getUserInfo(invalidUserId))
                .isInstanceOf(UserException.class)
                .hasMessage(UserExceptionErrorCode.USER_NOT_EXIST.getMessage());
    }
}

