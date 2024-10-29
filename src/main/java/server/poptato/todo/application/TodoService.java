package server.poptato.todo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.poptato.todo.api.request.DragAndDropRequestDto;
import server.poptato.todo.application.response.TodoDetailResponseDto;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.repository.TodoRepository;
import server.poptato.todo.domain.value.TodayStatus;
import server.poptato.todo.domain.value.Type;
import server.poptato.todo.exception.TodoException;
import server.poptato.todo.exception.errorcode.TodoExceptionErrorCode;
import server.poptato.user.validator.UserValidator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static server.poptato.todo.exception.errorcode.TodoExceptionErrorCode.TODO_NOT_EXIST;

@Transactional
@RequiredArgsConstructor
@Service
public class TodoService {
    private final TodoRepository todoRepository;
    private final UserValidator userValidator;


    public void deleteTodoById(Long userId, Long todoId) {
        userValidator.checkIsExistUser(userId);
        Todo todo = todoRepository.findByIdAndUserId(todoId, userId)
                .orElseThrow(() -> new TodoException(TODO_NOT_EXIST));

        todoRepository.delete(todo);
    }

    public void toggleIsBookmark(Long userId, Long todoId) {
        // 해당 Todo를 조회
        Todo todo = todoRepository.findByIdAndUserId(todoId, userId)
                .orElseThrow(() -> new TodoException(TODO_NOT_EXIST));

        // isBookmark 값을 토글하는 메서드 호출
        todo.toggleBookmark();
    }



    public void swipe(Long userId, Long todoId) {
        userValidator.checkIsExistUser(userId);
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new TodoException(TODO_NOT_EXIST));
        if (todo.getUserId() != userId)
            throw new TodoException(TodoExceptionErrorCode.TODO_USER_NOT_MATCH);

        if (todo.getType().equals(Type.TODAY)) {
            swipeTodayToBacklog(todo);
        } else if (todo.getType().equals(Type.YESTERDAY) || todo.getType().equals(Type.BACKLOG)) {
            swipeBacklogToToday(todo);
        }
    }

    public void dragAndDrop(Long userId, DragAndDropRequestDto requestDto) {
        userValidator.checkIsExistUser(userId);
        List<Todo> todos = getTodos(requestDto);
        checkIsValidToDragAndDrop(userId, todos, requestDto);
        if (requestDto.getType().equals(Type.TODAY)) {
            reassignTodayOrder(todos, requestDto.getTodoIds());
        } else if (requestDto.getType().equals(Type.BACKLOG)) {
            reassignBacklogOrder(todos, requestDto.getTodoIds());
        }
    }

    public TodoDetailResponseDto getTodoInfo(Long userId, Long todoId) {
        userValidator.checkIsExistUser(userId);
        Todo findTodo = todoRepository.findById(todoId)
                .orElseThrow(() -> new TodoException(TODO_NOT_EXIST));
        if (findTodo.getUserId() != userId)
            throw new TodoException(TodoExceptionErrorCode.TODO_USER_NOT_MATCH);

        return TodoDetailResponseDto.builder()
                .content(findTodo.getContent())
                .isBookmark(findTodo.isBookmark())
                .deadline(findTodo.getDeadline())
                .build();
    }

    public void updateDeadline(Long userId, Long todoId, LocalDate deadline) {
        userValidator.checkIsExistUser(userId);
        Todo findTodo = todoRepository.findById(todoId)
                .orElseThrow(() -> new TodoException(TODO_NOT_EXIST));
        if (findTodo.getUserId() != userId)
            throw new TodoException(TodoExceptionErrorCode.TODO_USER_NOT_MATCH);
        findTodo.updateDeadline(deadline);
        //TODO: 여기도 왜 SAVE가 필수인지 몰겟담
        todoRepository.save(findTodo);
    }

    public void updateContent(Long userId, Long todoId, String content) {
        userValidator.checkIsExistUser(userId);
        Todo findTodo = todoRepository.findById(todoId)
                .orElseThrow(() -> new TodoException(TODO_NOT_EXIST));
        if (findTodo.getUserId() != userId)
            throw new TodoException(TodoExceptionErrorCode.TODO_USER_NOT_MATCH);
        findTodo.updateContent(content);
        //TODO: 여기도 왜 SAVE가 필수인지 몰겟담
        todoRepository.save(findTodo);
    }

    public void updateIsCompleted(Long userId, Long todoId) {
        userValidator.checkIsExistUser(userId);
        Todo findTodo = todoRepository.findById(todoId)
                .orElseThrow(() -> new TodoException(TODO_NOT_EXIST));
        checkIsValidToUpdateIsCompleted(userId, findTodo);

        if (findTodo.getTodayStatus().equals(TodayStatus.COMPLETED)) {
            findTodo.updateTodayStatusToInComplete();
        } else {
            findTodo.updateTodayStatusToCompleted();
        }
    }

    private void swipeBacklogToToday(Todo todo) {
        Integer maxTodayOrder = todoRepository.findMaxTodayOrderByUserIdOrZero(todo.getUserId());
        todo.changeToToday(maxTodayOrder);
    }

    private void swipeTodayToBacklog(Todo todo) {
        if (todo.getTodayStatus().equals(TodayStatus.COMPLETED))
            throw new TodoException(TodoExceptionErrorCode.ALREADY_COMPLETED_TODO);
        Integer maxBacklogOrder = todoRepository.findMaxBacklogOrderByUserIdOrZero(todo.getUserId());
        todo.changeToBacklog(maxBacklogOrder);
    }

    private List<Todo> getTodos(DragAndDropRequestDto requestDto) {
        List<Todo> todos = new ArrayList<>();
        for (Long todoId : requestDto.getTodoIds()) {
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



    private void checkIsValidToUpdateIsCompleted(Long userId, Todo todo) {
        if (todo.getUserId() != userId)
            throw new TodoException(TodoExceptionErrorCode.TODO_USER_NOT_MATCH);
        if (todo.getType().equals(Type.BACKLOG))
            throw new TodoException(TodoExceptionErrorCode.BACKLOG_CANT_COMPLETE);
        if (todo.getType().equals(Type.YESTERDAY) && todo.getTodayStatus().equals(TodayStatus.COMPLETED))
            throw new TodoException(TodoExceptionErrorCode.YESTERDAY_CANT_COMPLETE);
    }
}
