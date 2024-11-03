package server.poptato.todo.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import server.poptato.todo.api.request.ContentUpdateRequestDto;
import server.poptato.todo.api.request.DeadlineUpdateRequestDto;
import server.poptato.todo.api.request.DragAndDropRequestDto;
import server.poptato.todo.api.request.SwipeRequestDto;
import server.poptato.todo.application.response.TodoDetailResponseDto;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.repository.TodoRepository;
import server.poptato.todo.domain.value.TodayStatus;
import server.poptato.todo.domain.value.Type;
import server.poptato.todo.exception.TodoException;
import server.poptato.todo.exception.errorcode.TodoExceptionErrorCode;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
class TodoServiceTest {
    @Autowired
    private TodoService todoService;
    @Autowired
    private TodoRepository todoRepository;

    @DisplayName("할일 삭제 시 성공한다.")
    @Test
    void delete_Success() {
        //given
        Long userId = 1L;
        Long todoId = 27L;

        //when
        todoService.deleteTodoById(userId, todoId);

        //then
        Optional<Todo> deletedTodo = todoRepository.findById(todoId);
        assertThat(deletedTodo).isEmpty();
    }

    @DisplayName("스와이프 시, 존재하지 않는 할 일이면 예외가 발생한다.")
    @Test
    void swipe_TodoNotExistException() {
        //given
        Long notExistTodoId = 1000L;
        Long userId = 1L;
        SwipeRequestDto swipeRequestDto = SwipeRequestDto.builder()
                .todoId(notExistTodoId)
                .build();

        //when & then
        assertThatThrownBy(() -> todoService.swipe(userId, swipeRequestDto))
                .isInstanceOf(TodoException.class)
                .hasMessage(TodoExceptionErrorCode.TODO_NOT_EXIST.getMessage());
    }

    @DisplayName("스와이프 시, 사용자의 할 일이 아닌 경우 예외가 발생한다.")
    @Test
    void swipe_TodoUserNotMatchException() {
        //given
        Long todoId = 1L;
        Long userId = 50L;
        SwipeRequestDto swipeRequestDto = SwipeRequestDto.builder()
                .todoId(todoId)
                .build();

        //when & then
        assertThatThrownBy(() -> todoService.swipe(userId, swipeRequestDto))
                .isInstanceOf(TodoException.class)
                .hasMessage(TodoExceptionErrorCode.TODO_USER_NOT_MATCH.getMessage());
    }

    @DisplayName("스와이프 시, 달성한 TODAY이면 예외가 발생한다.")
    @Test
    void swipe_AlreadyCompletedTodoException() {
        //given
        Long userId = 1L;
        Long todoId = 3L;
        SwipeRequestDto swipeRequestDto = SwipeRequestDto.builder()
                .todoId(todoId)
                .build();

        //when & then
        assertThatThrownBy(() -> todoService.swipe(userId, swipeRequestDto))
                .isInstanceOf(TodoException.class)
                .hasMessage(TodoExceptionErrorCode.ALREADY_COMPLETED_TODO.getMessage());
    }

    @DisplayName("스와이프 시, TODAY인 할일이면 BACKLOG로 성공적으로 수정된다.")
    @Test
    void swipe_Today_Success() {
        //given
        Long userId = 1L;
        Long todoId = 4L;
        Integer maxBacklogOrder = todoRepository.findMaxBacklogOrderByUserIdOrZero(userId);
        SwipeRequestDto swipeRequestDto = SwipeRequestDto.builder()
                .todoId(todoId)
                .build();

        //when
        todoService.swipe(userId, swipeRequestDto);
        Todo findTodo = todoRepository.findById(todoId).get();

        //then
        assertThat(findTodo.getType()).isEqualTo(Type.BACKLOG);
        assertThat(findTodo.getBacklogOrder()).isEqualTo(maxBacklogOrder + 1);
        assertThat(findTodo.getTodayOrder()).isNull();
        assertThat(findTodo.getTodayStatus()).isNull();
        assertThat(findTodo.getTodayDate()).isNull();
    }

    @DisplayName("스와이프 시, BACKLOG인 할일이면 TODAY로 성공적으로 수정된다.")
    @Test
    void swipe_Backlog_Success() {
        //given
        Long userId = 1L;
        Long todoId = 18L;
        Integer maxTodayOrder = todoRepository.findMaxTodayOrderByUserIdOrZero(userId);
        SwipeRequestDto swipeRequestDto = SwipeRequestDto.builder()
                .todoId(todoId)
                .build();

        //when
        todoService.swipe(userId, swipeRequestDto);
        Todo findTodo = todoRepository.findById(todoId).get();

        //then
        assertThat(findTodo.getType()).isEqualTo(Type.TODAY);
        assertThat(findTodo.getBacklogOrder()).isNull();
        assertThat(findTodo.getTodayOrder()).isEqualTo(maxTodayOrder + 1);
        assertThat(findTodo.getTodayStatus()).isEqualTo(TodayStatus.INCOMPLETE);
        assertThat(findTodo.getTodayDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("즐겨찾기 변경 시 isBookmark가 true이면 false로 변경된다.")
    void toggleIsBookmark_TrueToFalse_Success() {
        // given
        Long userId = 1L;
        Long todoId = 28L;

        // when
        todoService.toggleIsBookmark(userId, todoId);

        // then
        Todo updatedTodo = todoRepository.findById(todoId).get();
        assertThat(updatedTodo.isBookmark()).isFalse();
    }

    @Test
    @DisplayName("즐겨찾기 변경 시 isBookmark가 false이면 true로 변경된다.")
    void toggleIsBookmark_FalseToTrue_Success() {
        // given
        Long userId = 1L;
        Long todoId = 27L;

        // when
        todoService.toggleIsBookmark(userId, todoId);

        // then
        Todo updatedTodo = todoRepository.findById(todoId).get();
        assertThat(updatedTodo.isBookmark()).isTrue();
    }

    @DisplayName("드래그앤드롭 시 할 일이 Type과 맞지 않으면 예외가 발생한다.")
    @Test
    void dragAndDrop_TodoTypeNotMatchException() {
        //given
        Long userId = 1L;
        DragAndDropRequestDto request = DragAndDropRequestDto.builder()
                .type(Type.BACKLOG)
                .todoIds(List.of(1L))
                .build();

        //when & then
        assertThatThrownBy(() -> todoService.dragAndDrop(userId, request))
                .isInstanceOf(TodoException.class)
                .hasMessage(TodoExceptionErrorCode.TODO_TYPE_NOT_MATCH.getMessage());
    }

    @DisplayName("드래그앤드롭 시 이미 달성한 TODAY 포함 시 예외가 발생한다.")
    @Test
    void dragAndDrop_AlreadyCompletedTodoException() {
        //given
        Long userId = 1L;
        DragAndDropRequestDto request = DragAndDropRequestDto.builder()
                .type(Type.TODAY)
                .todoIds(List.of(3L))
                .build();

        //when & then
        assertThatThrownBy(() -> todoService.dragAndDrop(userId, request))
                .isInstanceOf(TodoException.class)
                .hasMessage(TodoExceptionErrorCode.ALREADY_COMPLETED_TODO.getMessage());
    }

    @DisplayName("드래그앤드롭 시 할 일들의 Order를 성공적으로 재정렬한다.")
    @Test
    void dragAndDrop_Success() {
        //given
        Long userId = 1L;
        DragAndDropRequestDto request = DragAndDropRequestDto.builder()
                .type(Type.TODAY)
                .todoIds(List.of(1L, 5L, 2L, 4L))
                .build();

        //when
        todoService.dragAndDrop(userId, request);

        //then
        Todo TodoId1 = todoRepository.findById(1L).get();
        Todo TodoId5 = todoRepository.findById(5L).get();
        Todo TodoId2 = todoRepository.findById(2L).get();
        Todo TodoId4 = todoRepository.findById(4L).get();

        assertThat(TodoId1.getTodayOrder()).isEqualTo(5L);
        assertThat(TodoId5.getTodayOrder()).isEqualTo(4L);
        assertThat(TodoId2.getTodayOrder()).isEqualTo(3L);
        assertThat(TodoId4.getTodayOrder()).isEqualTo(2L);
    }


    @DisplayName("할 일 상세 조회 시 성공한다.")
    @Test
    void getTodoInfo_Success() {
        //given
        Long userId = 1L;
        Long todoId = 10L;

        //when
        TodoDetailResponseDto todoInfo = todoService.getTodoInfo(userId, todoId);

        //then
        assertThat(todoInfo.content()).isEqualTo("할 일 10");
        assertThat(todoInfo.deadline()).isEqualTo(LocalDate.of(2024, 10, 26));
        assertThat(todoInfo.isBookmark()).isTrue();
    }

    @DisplayName("마감기한 수정 시 성공한다.")
    @Test
    void updateDeadline_Success() {
        //given
        Long userId = 1L;
        Long todoId = 11L;
        LocalDate updateDate = LocalDate.of(2024, 12, 25);
        DeadlineUpdateRequestDto deadlineUpdateRequestDto = DeadlineUpdateRequestDto.builder()
                .deadline(updateDate)
                .build();

        //when
        todoService.updateDeadline(userId, todoId, deadlineUpdateRequestDto);
        Todo findTodo = todoRepository.findById(todoId).get();

        //then
        assertThat(findTodo.getDeadline()).isEqualTo(updateDate);
    }

    @DisplayName("할 일 내용 수정 시 성공한다.")
    @Test
    void updateContent_Success() {
        //given
        Long userId = 1L;
        Long todoId = 11L;
        String updateContent = "할 일 내용 수정";
        ContentUpdateRequestDto contentUpdateRequestDto = ContentUpdateRequestDto.builder()
                .content(updateContent)
                .build();

        //when
        todoService.updateContent(userId, todoId, contentUpdateRequestDto);

        //then
        Todo findTodo = todoRepository.findById(todoId).get();
        assertThat(findTodo.getContent()).isEqualTo(updateContent);
    }

    @DisplayName("투데이 달성여부 변경 시 성공한다.")
    @Test
    void updateIsCompleted_Today_Success() {
        //given
        Long userId = 1L;
        Long todoId = 1L;

        //when
        todoService.updateIsCompleted(userId, todoId);
        Todo findTodo = todoRepository.findById(todoId).get();

        //then
        assertThat(findTodo.getTodayStatus()).isEqualTo(TodayStatus.COMPLETED);
        assertThat(findTodo.getCompletedDateTime()).isNotNull();
    }

    @DisplayName("어제한일 달성여부 변경 시 성공한다.")
    @Test
    void updateIsCompleted_Yesterday_Success() {
        //given
        Long userId = 1L;
        Long todoId = 35L;

        //when
        todoService.updateIsCompleted(userId, todoId);
        Todo findTodo = todoRepository.findById(todoId).get();

        //then
        assertThat(findTodo.getTodayStatus()).isEqualTo(TodayStatus.COMPLETED);
        assertThat(findTodo.getCompletedDateTime()).isNotNull();
    }

    @DisplayName("할 일 달성 여부 변경 시 백로그이면 예외가 발생한다.")
    @Test
    void updateIsCompleted_BacklogCantCompleteException() {
        //given
        Long userId = 1L;
        Long todoId = 20L;

        //when & then
        assertThatThrownBy(() -> todoService.updateIsCompleted(userId, todoId))
                .isInstanceOf(TodoException.class)
                .hasMessage(TodoExceptionErrorCode.BACKLOG_CANT_COMPLETE.getMessage());
    }
}