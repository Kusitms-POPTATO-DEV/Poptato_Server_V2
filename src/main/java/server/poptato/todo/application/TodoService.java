package server.poptato.todo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import server.poptato.todo.application.response.TodayListResponseDto;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.repository.TodoRepository;
import server.poptato.todo.domain.value.TodayStatus;
import server.poptato.todo.domain.value.Type;
import server.poptato.user.domain.repository.UserRepository;
import server.poptato.user.exception.UserException;
import server.poptato.user.exception.errorcode.UserExceptionErrorCode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TodoService {
    private final UserRepository userRepository;
    private final TodoRepository todoRepository;
    public TodayListResponseDto getTodayList(long userId, int page, int size) {
        checkIsExistUser(userId);

        LocalDate todayDate = LocalDate.now();
        PageRequest pageRequest = PageRequest.of(page, size);

        // 미완료된 할 일 먼저 조회
        Page<Todo> incompleteTodos = todoRepository.findByUserIdAndTypeAndTodayDateAndTodayStatusOrderByTodayOrderAsc(
                userId, Type.TODAY, todayDate, TodayStatus.INCOMPLETE, pageRequest);

        List<Todo> todays = new ArrayList<>(incompleteTodos.getContent());

        // 만약 미완료된 할 일이 size를 채우지 못하면, 완료된 할 일에서 남은 할 일만큼 가져옴
        if (todays.size() < size) {
            int remainingSize = size - todays.size();
            Pageable completedPageable = PageRequest.of(0, remainingSize);

            Page<Todo> completedTodos = todoRepository.findByUserIdAndTypeAndTodayDateAndTodayStatusOrderByCompletedDateTimeDesc(
                    userId, Type.TODAY, todayDate, TodayStatus.COMPLETED, completedPageable);

            todays.addAll(completedTodos.getContent());
        }

        return new TodayListResponseDto(todayDate,todays);
    }

    private void checkIsExistUser(long userId) {
        userRepository.findById(userId).orElseThrow(()
                -> new UserException(UserExceptionErrorCode.USER_NOT_EXIST));
    }
}
