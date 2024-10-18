package server.poptato.user.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import server.poptato.user.application.response.UserResponseDto;
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
    @Test
    @DisplayName("getUserNameAndEmailById 메서드는 유효한 userId가 주어졌을 때 사용자 이름과 이메일을 반환한다")
    public void shouldReturnUserNameAndEmail_WhenUserIdIsValid() {
        // Given
        Long userId = 1L;

        // When
        UserResponseDto responseDto = userService.getUserNameAndEmailById(userId);

        // Then
        assertThat(responseDto.getName()).isEqualTo("Poptato");
        assertThat(responseDto.getEmail()).isEqualTo("poptato@naver.com");
    }

    @Test
    @DisplayName("getUserNameAndEmailById 메서드는 유효하지 않은 userId가 주어졌을 때 UserException을 던진다")
    public void shouldThrowUserException_WhenUserIdIsInvalid() {
        // Given
        Long invalidUserId = 2L;

        // When & Then
        org.junit.jupiter.api.Assertions.assertThrows(UserException.class,
                () -> userService.getUserNameAndEmailById(invalidUserId));
    }
}

