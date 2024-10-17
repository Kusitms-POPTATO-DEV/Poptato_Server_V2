package server.poptato.user.application.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.poptato.auth.application.service.JwtService;
import server.poptato.todo.domain.repository.TodoRepository;
import server.poptato.user.domain.entity.User;
import server.poptato.user.domain.repository.UserRepository;
import server.poptato.user.exception.UserException;
import server.poptato.user.exception.errorcode.UserExceptionErrorCode;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TodoRepository todoRepository;
    private final JwtService jwtService;
    @Autowired
    private EntityManager entityManager;


    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(()
                -> new UserException(UserExceptionErrorCode.USER_NOT_EXIST));
        todoRepository.deleteAllByUserId(userId);
        jwtService.deleteRefreshToken(String.valueOf(userId));
        userRepository.delete(user);
        entityManager.flush();
    }
    @Transactional
    @CacheEvict(value = "users", key = "#kakaoId")
    public Optional<User> findUserByKakaoId(String kakaoId) {
        entityManager.clear();  // 영속성 컨텍스트 초기화
        return userRepository.findByKakaoId(kakaoId);
    }
    @Transactional
    public void updateUserName(Long userId, String newName) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserExceptionErrorCode.USER_NOT_EXIST));

        // name 업데이트
        user.changeName(newName);
    }
}
