package server.poptato.todo.application;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import server.poptato.todo.api.request.BacklogCreateRequestDto;
import server.poptato.todo.api.request.DragAndDropRequestDto;
import server.poptato.todo.api.request.SwipeRequestDto;
import org.springframework.transaction.annotation.Transactional;
import server.poptato.global.response.BaseResponse;
import server.poptato.todo.application.response.*;
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
import java.util.Optional;
import java.util.stream.Collectors;

import static server.poptato.todo.exception.errorcode.TodoExceptionErrorCode.TODO_NOT_EXIST;

@RequiredArgsConstructor
@Service
public class TodoService {
    private final UserRepository userRepository;
    private final TodoRepository todoRepository;
    public TodayListResponseDto getTodayList(long userId, int page, int size, LocalDate todayDate) {
        checkIsExistUser(userId);

        List<Todo> todays = new ArrayList<>();

        // 미완료된 할 일 먼저 조회
        List<Todo> incompleteTodos = todoRepository.findByUserIdAndTypeAndTodayDateAndTodayStatusOrderByTodayOrderDesc(
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

        Page<Todo> backlogs = todoRepository.findByUserIdAndTypeInOrderByBacklogOrderDesc(userId, types, pageRequest);

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
        //유저 아이디와 completedDate가 null 이 아닌것들을 가져옴
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

    @Transactional
    public void swipe(Long userId, Long todoId) {
        checkIsExistUser(userId);
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new TodoException(TODO_NOT_EXIST));
        if(todo.getUserId()!=userId)
            throw new TodoException(TodoExceptionErrorCode.TODO_USER_NOT_MATCH);

        if(todo.getType().equals(Type.TODAY)){
            swipeTodayToBacklog(todo);
        }
        else if(todo.getType().equals(Type.YESTERDAY) || todo.getType().equals(Type.BACKLOG)){
            swipeBacklogToToday(todo);
        }
    }
    public void dragAndDrop(Long userId, DragAndDropRequestDto requestDto) {
        checkIsExistUser(userId);
        List<Todo> todos = getTodos(requestDto);
        checkIsValidToDragAndDrop(userId,todos,requestDto);
        if (requestDto.getType().equals(Type.TODAY)) {
            reassignTodayOrder(todos, requestDto.getTodoIds());
        } else if (requestDto.getType().equals(Type.BACKLOG)) {
            reassignBacklogOrder(todos, requestDto.getTodoIds());
        }
    }
    public PaginatedYesterdayResponseDto getYesterdays(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Todo> todosPage = todoRepository.findByUserIdAndTypeAndTodayStatus(userId, Type.YESTERDAY, TodayStatus.INCOMPLETE, pageable);

        List<YesterdayResponseDto> yesterdays = todosPage.getContent().stream()
                .map(todo -> new YesterdayResponseDto(
                        todo.getId(),
                        todo.getContent()
                ))
                .collect(Collectors.toList());

        return new PaginatedYesterdayResponseDto(yesterdays, todosPage.getTotalPages());
    }

    private void swipeBacklogToToday(Todo todo) {
        Integer maxTodayOrder = todoRepository.findMaxTodayOrderByUserIdOrZero(todo.getUserId());
        todo.changeToToday(maxTodayOrder);
    }

    private void swipeTodayToBacklog(Todo todo) {
        if(todo.getTodayStatus().equals(TodayStatus.COMPLETED))
            throw new TodoException(TodoExceptionErrorCode.ALREADY_COMPLETED_TODO);
        Integer maxBacklogOrder = todoRepository.findMaxBacklogOrderByUserIdOrZero(todo.getUserId());
        todo.changeToBacklog(maxBacklogOrder);
    }

    private List<Todo> getTodos(DragAndDropRequestDto requestDto) {
        List<Todo> todos = new ArrayList<>();
        for(Long todoId: requestDto.getTodoIds()){
            todos.add(todoRepository.findById(todoId).get());
        }
        return todos;
    }

    private void checkIsValidToDragAndDrop(Long userId, List<Todo> todos, DragAndDropRequestDto requestDto) {
        if (todos.size() != requestDto.getTodoIds().size()) {
            throw new TodoException(TodoExceptionErrorCode.TODO_NOT_EXIST);
        }
        for (Todo todo : todos) {
            if (!todo.getUserId().equals(userId)) {
                throw new TodoException(TodoExceptionErrorCode.TODO_USER_NOT_MATCH);
            }
            if (requestDto.getType().equals(Type.TODAY) && todo.getTodayStatus() == TodayStatus.COMPLETED) {
                throw new TodoException(TodoExceptionErrorCode.ALREADY_COMPLETED_TODO);
            }
            if (requestDto.getType().equals(Type.TODAY)) {
                if (!todo.getType().equals(Type.TODAY)) {
                    throw new TodoException(TodoExceptionErrorCode.TODO_TYPE_NOT_MATCH);
                }
            }
            if (requestDto.getType().equals(Type.BACKLOG)) {
                if (!(todo.getType().equals(Type.BACKLOG) || todo.getType().equals(Type.YESTERDAY))) {
                    throw new TodoException(TodoExceptionErrorCode.TODO_TYPE_NOT_MATCH);
                }
            }
        }
    }

    private void reassignTodayOrder(List<Todo> todos, List<Long> todoIds) {
        int startingOrder = todoRepository.findMaxTodayOrderByIdIn(todoIds);
        for (Todo todo : todos) {
            todo.setTodayOrder(startingOrder--);
            //TODO: 왜 save를 호출해야지 반영이 되는지 알아야함
            todoRepository.save(todo);
        }
    }

    private void reassignBacklogOrder(List<Todo> todos, List<Long> todoIds) {
        int startingOrder = todoRepository.findMaxBacklogOrderByIdIn(todoIds);
        for (Todo todo : todos) {
            todo.setBacklogOrder(startingOrder--);
            todoRepository.save(todo);
        }
    }

    private void checkIsExistUser(Long userId) {
        userRepository.findById(userId).orElseThrow(()
                -> new UserException(UserExceptionErrorCode.USER_NOT_EXIST));
    }

    public Long generateBacklog(Long userId, String content) {
        checkIsExistUser(userId);
        Integer maxBacklogOrder = todoRepository.findMaxBacklogOrderByUserIdOrZero(userId);
        Todo backlog = Todo.createBacklog(userId, content, maxBacklogOrder+1);
        Todo newBacklog = todoRepository.save(backlog);
        return newBacklog.getId();
    }

    public TodoDetailResponseDto getTodoInfo(Long userId, Long todoId) {
        checkIsExistUser(userId);
        Todo findTodo = todoRepository.findById(todoId)
                .orElseThrow(() -> new TodoException(TODO_NOT_EXIST));
        if(findTodo.getUserId()!=userId)
            throw new TodoException(TodoExceptionErrorCode.TODO_USER_NOT_MATCH);

        return TodoDetailResponseDto.builder()
                    .content(findTodo.getContent())
                    .isBookmark(findTodo.isBookmark())
                    .deadline(findTodo.getDeadline())
                    .build();
    }
}
