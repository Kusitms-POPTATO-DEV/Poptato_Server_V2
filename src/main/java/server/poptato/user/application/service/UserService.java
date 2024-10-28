package server.poptato.user.application.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.poptato.auth.application.service.JwtService;
import server.poptato.todo.domain.repository.TodoRepository;
import server.poptato.user.application.response.UserResponseDto;
import server.poptato.user.domain.entity.User;
import server.poptato.user.domain.repository.UserRepository;
import server.poptato.user.exception.UserException;
import server.poptato.user.exception.errorcode.UserExceptionErrorCode;

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
        User user = checkIsExistUser(userId);
        todoRepository.deleteAllByUserId(userId);
        jwtService.deleteRefreshToken(String.valueOf(userId));
        userRepository.delete(user);
        entityManager.flush();
    }
    @Transactional(readOnly = true)
    public UserResponseDto getUserNameAndEmailById(Long userId) {
        User user = checkIsExistUser(userId);
        return new UserResponseDto(user.getName(), user.getEmail());
    }
    private User checkIsExistUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserExceptionErrorCode.USER_NOT_EXIST));
    }
}
