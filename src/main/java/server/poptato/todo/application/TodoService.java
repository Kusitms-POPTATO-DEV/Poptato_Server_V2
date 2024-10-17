package server.poptato.todo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.poptato.global.response.BaseResponse;
import server.poptato.todo.application.response.BacklogListResponseDto;
import server.poptato.todo.application.response.HistoryResponseDto;
import server.poptato.todo.application.response.PaginatedHistoryResponseDto;
import server.poptato.todo.application.response.TodayListResponseDto;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.repository.TodoRepository;
import server.poptato.todo.domain.value.TodayStatus;
import server.poptato.todo.domain.value.Type;
import server.poptato.todo.exception.TodoException;
import server.poptato.todo.exception.errorcode.TodoExceptionErrorCode;
import server.poptato.user.domain.repository.UserRepository;
import server.poptato.user.exception.UserException;
import server.poptato.user.exception.errorcode.UserExceptionErrorCode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static server.poptato.todo.exception.errorcode.TodoExceptionErrorCode.TODO_NOT_EXIST;

@RequiredArgsConstructor
@Service
public class TodoService {
    private final UserRepository userRepository;
    private final TodoRepository todoRepository;
    public TodayListResponseDto getTodayList(long userId, int page, int size) {
        checkIsExistUser(userId);

        LocalDate todayDate = LocalDate.now();
        List<Todo> todays = new ArrayList<>();

        // 미완료된 할 일 먼저 조회
        List<Todo> incompleteTodos = todoRepository.findByUserIdAndTypeAndTodayDateAndTodayStatusOrderByTodayOrderAsc(
                userId, Type.TODAY, todayDate, TodayStatus.INCOMPLETE);
        todays.addAll(incompleteTodos);

        // 완료된 할 일 조회
        List<Todo> completedTodos = todoRepository.findByUserIdAndTypeAndTodayDateAndTodayStatusOrderByCompletedDateTimeDesc(
                    userId, Type.TODAY, todayDate, TodayStatus.COMPLETED);
        todays.addAll(completedTodos);

        // 전체 리스트에서 페이징
        int start = (page) * size;
        int end = Math.min(start + size, todays.size());
        if (start >= end) throw new TodoException(TodoExceptionErrorCode.INVALID_PAGE);

        List<Todo> todaySubList = todays.subList(start, end);
        int totalPageCount = (int) Math.ceil((double) todays.size() / size);

        return TodayListResponseDto.builder()
                .date(todayDate)
                .todays(todaySubList)
                .totalPageCount(totalPageCount)
                .build();
    }

    public BacklogListResponseDto getBacklogList(Long userId, int page, int size) {
        checkIsExistUser(userId);

        PageRequest pageRequest = PageRequest.of(page, size);
        List<Type> types = List.of(Type.BACKLOG, Type.YESTERDAY);

        Page<Todo> backlogs = todoRepository.findByUserIdAndTypeInOrderByBacklogOrderAsc(userId, types, pageRequest);

       return BacklogListResponseDto.builder()
               .totalCount(backlogs.getTotalElements())
               .backlogs(backlogs.getContent())
               .totalPageCount(backlogs.getTotalPages())
               .build();
    }

    @Transactional
    public void deleteTodoById(Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new TodoException(TODO_NOT_EXIST));
        todoRepository.delete(todo);
    }
    @Transactional
    public void toggleIsBookmark(Long todoId) {
        // 해당 Todo를 조회
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new TodoException(TODO_NOT_EXIST));

        // isBookmark 값을 토글하는 메서드 호출
        todo.toggleBookmark();
    }

    public PaginatedHistoryResponseDto getHistories(Long userId, int page, int size) {
        checkIsExistUser(userId);
        Pageable pageable = PageRequest.of(page, size);
        //유정 아이디와 completedDate가 null 이 아닌것들을 가져옴
        Page<Todo> todosPage = todoRepository.findByUserIdAndCompletedDateTimeIsNotNull(userId, pageable);

        List<HistoryResponseDto> histories = todosPage.getContent().stream()
                .sorted(Comparator.comparing(todo -> todo.getCompletedDateTime().toLocalDate()))  // 날짜 오름차순 정렬
                .map(todo -> new HistoryResponseDto(
                        todo.getId(),
                        todo.getContent(),
                        todo.getCompletedDateTime().toLocalDate()  // 날짜를 localdate로 변환
                ))
                .collect(Collectors.toList());

        return new PaginatedHistoryResponseDto(histories, todosPage.getTotalPages());
    }

    private void checkIsExistUser(long userId) {
        userRepository.findById(userId).orElseThrow(()
                -> new UserException(UserExceptionErrorCode.USER_NOT_EXIST));
    }
}
