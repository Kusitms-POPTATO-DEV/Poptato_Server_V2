package server.poptato.user.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import server.poptato.user.application.service.UserService;
import server.poptato.user.domain.entity.User;
import server.poptato.user.domain.repository.UserRepository;
import server.poptato.user.exception.UserException;
import server.poptato.user.exception.errorcode.UserExceptionErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자 이름 변경 성공 테스트")
    @Transactional
    void updateUserName_ShouldChangeUserName() {
        // given
        Long userId = 1L;
        String newName = "New Name";

        // when
        userService.updateUserName(userId, newName);

        // then
        User updatedUser = userRepository.findById(userId).orElseThrow();
        assertThat(updatedUser.getName()).isEqualTo(newName);
    }

    @Test
    @DisplayName("존재하지 않는 사용자일 경우 예외 발생 테스트")
    @Transactional
    void updateUserName_ShouldThrowException_WhenUserNotFound() {
        // given
        Long nonExistentUserId = 999L;  // 존재하지 않는 유저 ID
        String newName = "New Name";

        // when & then
        UserException exception = assertThrows(UserException.class, () -> {
            userService.updateUserName(nonExistentUserId, newName);
        });

        assertThat(exception.getMessage()).isEqualTo(UserExceptionErrorCode.USER_NOT_EXIST.getMessage());
    }
}

