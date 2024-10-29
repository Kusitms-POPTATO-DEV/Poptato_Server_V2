package server.poptato.user.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.poptato.auth.application.service.JwtService;
import server.poptato.todo.domain.repository.TodoRepository;
import server.poptato.user.application.response.UserInfoResponseDto;
import server.poptato.user.domain.entity.User;
import server.poptato.user.domain.repository.UserRepository;
import server.poptato.user.validator.UserValidator;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TodoRepository todoRepository;
    private final JwtService jwtService;
    private final UserValidator userValidator;

    public void deleteUser(Long userId) {
        User user = userValidator.checkIsExistAndReturnUser(userId);
        todoRepository.deleteAllByUserId(userId);
        jwtService.deleteRefreshToken(String.valueOf(userId));
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public UserInfoResponseDto getUserInfo(Long userId) {
        User user = userValidator.checkIsExistAndReturnUser(userId);
        return new UserInfoResponseDto(user.getName(), user.getEmail());
    }
}
