package server.poptato.user.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.poptato.auth.application.service.JwtService;
import server.poptato.global.exception.BaseException;
import server.poptato.user.domain.entity.User;
import server.poptato.user.infra.repository.JpaUserRepository;

import static server.poptato.global.exception.errorcode.BaseExceptionErrorCode.USER_NOT_FOUND_EXCEPTION;

@Service
@RequiredArgsConstructor
public class UserService {
    private final JpaUserRepository userRepository;
    private final TodoRepository todoRepository;
    private final JwtService jwtService;

    @Transactional
    public void deleteUser(Long userId) {
        // 유저 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(USER_NOT_FOUND_EXCEPTION);

        // 유저와 연관된 모든 Todo 삭제
        todoRepository.deleteAll(todoRepository.findAllByUserId(userId));

        // Redis에서 리프레시 토큰 삭제
        jwtService.deleteRefreshToken(String.valueOf(userId));

        // 유저 삭제
        userRepository.delete(user);
    }
}
