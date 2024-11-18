package server.poptato.todo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.poptato.category.validator.CategoryValidator;
import server.poptato.todo.api.request.*;
import server.poptato.todo.application.response.HistoryCalendarListResponseDto;
import server.poptato.todo.application.response.PaginatedHistoryResponseDto;
import server.poptato.todo.application.response.TodoDetailResponseDto;
import server.poptato.todo.converter.TodoDtoConverter;
import server.poptato.todo.domain.entity.CompletedDateTime;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.repository.CompletedDateTimeRepository;
import server.poptato.todo.domain.repository.TodoRepository;
import server.poptato.todo.domain.value.TodayStatus;
import server.poptato.todo.domain.value.Type;
import server.poptato.todo.exception.TodoException;
import server.poptato.todo.exception.errorcode.TodoExceptionErrorCode;
import server.poptato.user.validator.UserValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static server.poptato.todo.exception.errorcode.TodoExceptionErrorCode.COMPLETED_DATETIME_NOT_EXIST;
import static server.poptato.todo.exception.errorcode.TodoExceptionErrorCode.TODO_NOT_EXIST;

@Transactional
@RequiredArgsConstructor
@Service
public class TodoService {
    private final TodoRepository todoRepository;
    private final CompletedDateTimeRepository completedDateTimeRepository;
    private final UserValidator userValidator;
    private final CategoryValidator categoryValidator;


    public void deleteTodoById(Long userId, Long todoId) {
        userValidator.checkIsExistUser(userId);
        Todo findTodo = validateAndReturnTodo(userId, todoId);
        todoRepository.delete(findTodo);
    }

    private Todo validateAndReturnTodo(Long userId, Long todoId) {
        Todo findTodo = todoRepository.findById(todoId)
                .orElseThrow(() -> new TodoException(TODO_NOT_EXIST));
        if (findTodo.getUserId() != userId)
            throw new TodoException(TodoExceptionErrorCode.TODO_USER_NOT_MATCH);
        return findTodo;
    }

    public void toggleIsBookmark(Long userId, Long todoId) {
        Todo todo = validateAndReturnTodo(userId, todoId);
        todo.toggleBookmark();
    }


    public void swipe(Long userId, SwipeRequestDto swipeRequestDto) {
        userValidator.checkIsExistUser(userId);
        Todo findTodo = validateAndReturnTodo(userId, swipeRequestDto.getTodoId());
        if (isToday(findTodo)) {
            swipeTodayToBacklog(findTodo);
            return;
        }
        swipeBacklogToToday(findTodo);
    }

    private boolean isToday(Todo findTodo) {
        return findTodo.getType().equals(Type.TODAY);
    }

    private void swipeBacklogToToday(Todo todo) {
        Integer maxTodayOrder = todoRepository.findMaxTodayOrderByUserIdOrZero(todo.getUserId());
        todo.changeToToday(maxTodayOrder);
    }

    private void swipeTodayToBacklog(Todo todo) {
        if (isCompletedTodo(todo))
            throw new TodoException(TodoExceptionErrorCode.ALREADY_COMPLETED_TODO);
        Integer maxBacklogOrder = todoRepository.findMaxBacklogOrderByUserIdOrZero(todo.getUserId());
        todo.changeToBacklog(maxBacklogOrder);
    }

    private boolean isCompletedTodo(Todo todo) {
        return todo.getTodayStatus().equals(TodayStatus.COMPLETED);
    }


    public void dragAndDrop(Long userId, DragAndDropRequestDto requestDto) {
        userValidator.checkIsExistUser(userId);
        List<Todo> todos = getTodosByIds(requestDto.getTodoIds());
        checkIsValidToDragAndDrop(userId, todos, requestDto);
        if (isTypeToday(requestDto.getType())) {
            reassignTodayOrder(todos, requestDto.getTodoIds());
            return;
        }
        reassignBacklogOrder(todos, requestDto.getTodoIds());
    }

    private boolean isTypeToday(Type type) {
        return type.equals(Type.TODAY);
    }

    private void reassignTodayOrder(List<Todo> todos, List<Long> todoIds) {
        int startingOrder = todoRepository.findMaxTodayOrderByIdIn(todoIds);
        for (Todo todo : todos) {
            todo.setTodayOrder(startingOrder--);
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

    private List<Todo> getTodosByIds(List<Long> todoIds) {
        List<Todo> todos = new ArrayList<>();
        for (Long todoId : todoIds) {
            todos.add(todoRepository.findById(todoId).get());
        }
        return todos;
    }

    private void checkIsValidToDragAndDrop(Long userId, List<Todo> todos, DragAndDropRequestDto dragAndDropRequestDto) {
        if (todos.size() != dragAndDropRequestDto.getTodoIds().size()) {
            throw new TodoException(TodoExceptionErrorCode.TODO_NOT_EXIST);
        }
        for (Todo todo : todos) {
            if (!todo.getUserId().equals(userId)) {
                throw new TodoException(TodoExceptionErrorCode.TODO_USER_NOT_MATCH);
            }
            if (dragAndDropRequestDto.getType().equals(Type.TODAY) && todo.getTodayStatus() == TodayStatus.COMPLETED) {
                throw new TodoException(TodoExceptionErrorCode.ALREADY_COMPLETED_TODO);
            }
            if (dragAndDropRequestDto.getType().equals(Type.TODAY)) {
                if (!todo.getType().equals(Type.TODAY)) {
                    throw new TodoException(TodoExceptionErrorCode.TODO_TYPE_NOT_MATCH);
                }
            }
            if (dragAndDropRequestDto.getType().equals(Type.BACKLOG)) {
                if (!(todo.getType().equals(Type.BACKLOG) || todo.getType().equals(Type.YESTERDAY))) {
                    throw new TodoException(TodoExceptionErrorCode.TODO_TYPE_NOT_MATCH);
                }
            }
        }
    }

    public TodoDetailResponseDto getTodoInfo(Long userId, Long todoId) {
        userValidator.checkIsExistUser(userId);
        Todo findTodo = validateAndReturnTodo(userId, todoId);
        return TodoDtoConverter.toTodoDetailInfoDto(findTodo);
    }

    public void updateDeadline(Long userId, Long todoId, DeadlineUpdateRequestDto deadlineUpdateRequestDto) {
        userValidator.checkIsExistUser(userId);
        Todo findTodo = validateAndReturnTodo(userId, todoId);
        findTodo.updateDeadline(deadlineUpdateRequestDto.getDeadline());
        todoRepository.save(findTodo);
    }

    public void updateContent(Long userId, Long todoId, ContentUpdateRequestDto contentUpdateRequestDto) {
        userValidator.checkIsExistUser(userId);
        Todo findTodo = validateAndReturnTodo(userId, todoId);
        findTodo.updateContent(contentUpdateRequestDto.getContent());
        todoRepository.save(findTodo);
    }

    public void updateIsCompleted(Long userId, Long todoId, LocalDateTime now) {
        userValidator.checkIsExistUser(userId);
        Todo findTodo = validateAndReturnTodo(userId, todoId);
        checkIsValidToUpdateIsCompleted(findTodo);

        if (isStatusCompleted(findTodo)) {
            Integer minTodayOrder = todoRepository.findMinTodayOrderByUserIdOrZero(userId);
            findTodo.updateTodayStatusToInComplete(minTodayOrder);
            CompletedDateTime completedDateTime = completedDateTimeRepository.findByDateAndTodoId(findTodo.getId(), now.toLocalDate())
                    .orElseThrow(() -> new TodoException(COMPLETED_DATETIME_NOT_EXIST));
            completedDateTimeRepository.delete(completedDateTime);
            return;
        }
        if (isTypeYesterday(findTodo)) {
            findTodo.updateYesterdayStatusToCompleted();
            CompletedDateTime completedDateTime = CompletedDateTime.builder().todoId(findTodo.getId()).dateTime(now).build();
            completedDateTimeRepository.save(completedDateTime);
            return;
        }
        findTodo.updateTodayStatusToCompleted();
        CompletedDateTime completedDateTime = CompletedDateTime.builder().todoId(findTodo.getId()).dateTime(now).build();
        completedDateTimeRepository.save(completedDateTime);
    }

    private void checkIsValidToUpdateIsCompleted(Todo todo) {
        if (todo.getType().equals(Type.BACKLOG))
            throw new TodoException(TodoExceptionErrorCode.BACKLOG_CANT_COMPLETE);
        if (todo.getType().equals(Type.YESTERDAY) && todo.getTodayStatus().equals(TodayStatus.COMPLETED))
            throw new TodoException(TodoExceptionErrorCode.YESTERDAY_CANT_COMPLETE);
    }

    private boolean isTypeYesterday(Todo findTodo) {
        return findTodo.getType().equals(Type.YESTERDAY);
    }

    private boolean isStatusCompleted(Todo findTodo) {
        return findTodo.getTodayStatus().equals(TodayStatus.COMPLETED);
    }

    public PaginatedHistoryResponseDto getHistories(Long userId, LocalDate localDate, int page, int size) {
        userValidator.checkIsExistUser(userId);
        Page<Todo> historiesPage = getHistoriesPage(userId, localDate, page, size);
        return TodoDtoConverter.toHistoryListDto(historiesPage);
    }

    private Page<Todo> getHistoriesPage(Long userId, LocalDate localDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Todo> historiesPage = todoRepository.findHistories(userId, localDate, pageable);
        return historiesPage;
    }

    public HistoryCalendarListResponseDto getHistoriesCalendar(Long userId, String year, int month) {
        List<LocalDateTime> dateTimes = completedDateTimeRepository.findHistoryExistingDates(userId, year, month);
        List<LocalDate> dates = dateTimes.stream()
                .map(LocalDateTime::toLocalDate)
                .toList();
        return HistoryCalendarListResponseDto.builder().dates(dates).build();
    }

    public void updateCategory(Long userId, Long todoId, TodoCategoryUpdateRequestDto requestDto) {
        userValidator.checkIsExistUser(userId);
        Todo findTodo = validateAndReturnTodo(userId, todoId);
        if(requestDto.categoryId()!=null) categoryValidator.validateCategory(userId, requestDto.categoryId());
        findTodo.updateCategory(requestDto.categoryId());
        todoRepository.save(findTodo);
    }
}
