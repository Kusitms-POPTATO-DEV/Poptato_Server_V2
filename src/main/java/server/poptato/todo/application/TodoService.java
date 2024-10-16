package server.poptato.todo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import server.poptato.todo.api.response.TodayListResponseDto;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.user.domain.repository.UserRepository;
import server.poptato.user.exception.UserException;
import server.poptato.user.exception.errorcode.UserExceptionErrorCode;

@RequiredArgsConstructor
@Service
public class TodoService {
    private final UserRepository userRepository;
    public TodayListResponseDto getTodayList(long userId, int page, int size) {
        checkIsExistUser(userId);

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Todo> todays;
        todays = todoRepository.find

        return null;
    }

    private void checkIsExistUser(long userId) {
        userRepository.findById(userId).orElseThrow(()
                -> new UserException(UserExceptionErrorCode.USER_NOT_EXIST));
    }
}
