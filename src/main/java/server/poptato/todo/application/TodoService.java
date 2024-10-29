package server.poptato.todo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.poptato.todo.api.request.ContentUpdateRequestDto;
import server.poptato.todo.api.request.DeadlineUpdateRequestDto;
import server.poptato.todo.api.request.DragAndDropRequestDto;
import server.poptato.todo.api.request.SwipeRequestDto;
import server.poptato.todo.application.response.TodoDetailResponseDto;
import server.poptato.todo.converter.TodoDtoConverter;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.repository.TodoRepository;
import server.poptato.todo.domain.value.TodayStatus;
import server.poptato.todo.domain.value.Type;
import server.poptato.todo.exception.TodoException;
import server.poptato.todo.exception.errorcode.TodoExceptionErrorCode;
import server.poptato.user.validator.UserValidator;

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
        Todo findTodo = validateAndReturnTodo(userId, todoId);
        todoRepository.delete(findTodo);
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

    public TodoDetailResponseDto getTodoInfo(Long userId, Long todoId) {
        userValidator.checkIsExistUser(userId);
        Todo findTodo = validateAndReturnTodo(userId, todoId);
        return TodoDtoConverter.toTodoDetailInfoDto(findTodo);
    }

    public void updateDeadline(Long userId, Long todoId, DeadlineUpdateRequestDto deadlineUpdateRequestDto) {
        userValidator.checkIsExistUser(userId);
        Todo findTodo = validateAndReturnTodo(userId, todoId);
        findTodo.updateDeadline(deadlineUpdateRequestDto.getDeadline());
        //TODO: 여기도 왜 SAVE가 필수인지 몰겟담
        todoRepository.save(findTodo);
    }

    public void updateContent(Long userId, Long todoId, ContentUpdateRequestDto contentUpdateRequestDto) {
        userValidator.checkIsExistUser(userId);
        Todo findTodo = validateAndReturnTodo(userId, todoId);
        findTodo.updateContent(contentUpdateRequestDto.getContent());
        //TODO: 여기도 왜 SAVE가 필수인지 몰겟담
        todoRepository.save(findTodo);
    }

    public void updateIsCompleted(Long userId, Long todoId) {
        userValidator.checkIsExistUser(userId);
        Todo findTodo = validateAndReturnTodo(userId, todoId);
        checkIsValidToUpdateIsCompleted(findTodo);

        if (isStatusCompleted(findTodo)) {
            Integer minTodayOrder = todoRepository.findMinTodayOrderByUserIdOrZero(userId);
            findTodo.updateTodayStatusToInComplete(minTodayOrder);
            return;
        }
        findTodo.updateTodayStatusToCompleted();
    }

    private boolean isStatusCompleted(Todo findTodo) {
        return findTodo.getTodayStatus().equals(TodayStatus.COMPLETED);
    }

    private Todo validateAndReturnTodo(Long userId, Long todoId) {
        Todo findTodo = todoRepository.findById(todoId)
                .orElseThrow(() -> new TodoException(TODO_NOT_EXIST));
        if (findTodo.getUserId() != userId)
            throw new TodoException(TodoExceptionErrorCode.TODO_USER_NOT_MATCH);
        return findTodo;
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


    private void checkIsValidToUpdateIsCompleted(Todo todo) {
        if (todo.getType().equals(Type.BACKLOG))
            throw new TodoException(TodoExceptionErrorCode.BACKLOG_CANT_COMPLETE);
        if (todo.getType().equals(Type.YESTERDAY) && todo.getTodayStatus().equals(TodayStatus.COMPLETED))
            throw new TodoException(TodoExceptionErrorCode.YESTERDAY_CANT_COMPLETE);
    }


    private boolean isToday(Todo findTodo) {
        return findTodo.getType().equals(Type.TODAY);
    }


    private boolean isTypeToday(Type type) {
        return type.equals(Type.TODAY);
    }
}
