package server.poptato.user.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import server.poptato.user.application.response.UserInfoResponseDto;
import server.poptato.user.application.service.UserService;
import server.poptato.user.domain.repository.UserRepository;
import server.poptato.user.exception.UserException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    @Test
    @DisplayName("getUserNameAndEmailById 메서드는 유효한 userId가 주어졌을 때 사용자 이름과 이메일을 반환한다")
    public void shouldReturnUserNameAndEmail_WhenUserIdIsValid() {
        // Given
        Long userId = 1L;

        // When
        UserInfoResponseDto responseDto = userService.getUserInfo(userId);

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
                () -> userService.getUserInfo(invalidUserId));
    }
}

