package server.poptato.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import server.poptato.auth.application.service.JwtService;
import server.poptato.global.exception.BaseException;
import server.poptato.todo.infra.repository.JpaTodoRepository;
import server.poptato.user.application.service.UserService;
import server.poptato.user.domain.entity.User;
import server.poptato.user.infra.repository.JpaUserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class UserServiceTest {

    @MockBean
    private JpaUserRepository userRepository;

    @MockBean
    private JpaTodoRepository todoRepository;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("유저 탈퇴 - 성공")
    public void deleteUser_success() {
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .kakaoId("kakao123")
                .name("Kyounglin")
                .email("Kyounglin@example.com")
                .build();

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        Mockito.verify(todoRepository).deleteAll(Mockito.anyList());
        Mockito.verify(jwtService).deleteRefreshToken(String.valueOf(userId));
        Mockito.verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("유저 탈퇴 - 실패 (존재하지 않는 유저)")
    public void deleteUser_notFound() {
        Long userId = 1L;

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(BaseException.class, () -> userService.deleteUser(userId));
        Mockito.verify(todoRepository, Mockito.never()).deleteAll(Mockito.anyList());
        Mockito.verify(jwtService, Mockito.never()).deleteRefreshToken(String.valueOf(userId));
        Mockito.verify(userRepository, Mockito.never()).delete(Mockito.any());
    }
}