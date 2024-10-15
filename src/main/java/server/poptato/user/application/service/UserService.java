package server.poptato.user.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.poptato.auth.application.service.JwtService;
import server.poptato.global.exception.BaseException;
import server.poptato.todo.domain.repository.TodoRepository;
import server.poptato.todo.infra.repository.JpaTodoRepository;
import server.poptato.user.domain.entity.User;
import server.poptato.user.domain.repository.UserRepository;
import server.poptato.user.infra.repository.JpaUserRepository;

import static server.poptato.global.exception.errorcode.BaseExceptionErrorCode.USER_NOT_FOUND_EXCEPTION;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TodoRepository todoRepository;
    private final JwtService jwtService;

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(USER_NOT_FOUND_EXCEPTION));
        todoRepository.deleteAll(todoRepository.findAllByUserId(userId));
        jwtService.deleteRefreshToken(String.valueOf(userId));
        userRepository.delete(user);
    }
}
