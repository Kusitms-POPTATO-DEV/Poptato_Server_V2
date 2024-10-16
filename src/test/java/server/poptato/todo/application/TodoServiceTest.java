package server.poptato.todo.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import server.poptato.todo.application.response.TodayListResponseDto;
import server.poptato.todo.application.response.TodayResponseDto;
import server.poptato.todo.domain.repository.TodoRepository;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.value.TodayStatus;
import server.poptato.todo.domain.value.Type;
import server.poptato.todo.exception.TodoException;
import server.poptato.todo.exception.errorcode.TodoExceptionErrorCode;
import server.poptato.user.exception.UserException;
import server.poptato.user.exception.errorcode.UserExceptionErrorCode;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static server.poptato.todo.exception.errorcode.TodoExceptionErrorCode.TODO_NOT_EXIST;

@SpringBootTest
class TodoServiceTest {
    @Autowired
    private TodoService todoService;
    @Autowired
    private TodoRepository todoRepository;


    @DisplayName("존재하지 않는 유저일 경우 예외가 발생한다.")
    @Test
    void 해당하는_유저정보_없는_예외(){
        //given
        Long notExistUserId = 100L;
        int page = 0;
        int size = 8;
        //when & then
        assertThatThrownBy(()-> todoService.getTodayList(notExistUserId,page,size))
                .isInstanceOf(UserException.class)
                .hasMessage(UserExceptionErrorCode.USER_NOT_EXIST.getMessage());
    }
    @DisplayName("size=8을 요청하면, 투데이 목록 조회 시 8개의 데이터만 응답된다.")
    @Test
    void 투데이_데이터_8개만_응답(){
        //given
        Long userId = 1L;
        int page = 0;
        int size = 8;
        //when & then
        assertThat(todoService.getTodayList(userId,page,size).getTodays().size()).isEqualTo(size);
    }

    @DisplayName("유효하지 않는 페이지 수일 경우 예외가 발생한다.")
    @Test
    void 유효하지_않은_페이지_수_예외(){
        //given
        Long userId = 1L;
        int page = 2;
        int size = 8;
        //when & then
        assertThatThrownBy(()-> todoService.getTodayList(userId,page,size))
                .isInstanceOf(TodoException.class)
                .hasMessage(TodoExceptionErrorCode.INVALID_PAGE.getMessage());
    }

    @DisplayName("투데이 목록 조회 시, 미달성 투데이가 먼저 조회되고, 그 다음 달성 투데이가 조회된다.")
    @Test
    void 투데이_목록_조회_달성여부_정렬(){
        //given
        Long userId = 1L;
        int page = 0;
        int size = 8;

        //when
        TodayListResponseDto todayList = todoService.getTodayList(userId, page, size);

        //then
        boolean foundCompleted = false;
        for (TodayResponseDto today : todayList.getTodays()) {
            if (today.getTodayStatus() == TodayStatus.COMPLETED) {
                foundCompleted = true;
            }
            // COMPLETED가 나오기 시작한 이후에는 INCOMPLETE가 나와서는 안됨
            if (foundCompleted) {
                assertThat(today.getTodayStatus()).isEqualTo(TodayStatus.COMPLETED);
            }
        }
    }
    // 새로운 유저로 삭제 테스트
    @DisplayName("투두가 있을 때 유저 2의 투두를 삭제한다.")
    @Test
    public void shouldDeleteTodoById_WhenTodoExists_ForUser2() {
        //given
        Long userId = 2L;
        Todo todo = Todo.builder()
                .userId(userId)
                .content("Test Todo")
                .type(Type.BACKLOG)
                .todayStatus(TodayStatus.COMPLETED)
                .build();

        // 실제로 Todo 저장
        Todo savedTodo = todoRepository.save(todo);
        Long todoId = savedTodo.getId();

        //when
        todoService.deleteTodoById(todoId);

        //then
        Optional<Todo> deletedTodo = todoRepository.findById(todoId);
        assertThat(deletedTodo).isEmpty();  // 삭제되었는지 검증
    }

    @DisplayName("투두가 없을 때 유저 2의 투두를 삭제할 때 예외가 발생한다.")
    @Test
    public void shouldThrowException_WhenTodoNotFound_ForUser2() {
        //given
        Long nonExistentTodoId = 30L;  // 존재하지 않는 투두 ID

        //when & then
        assertThrows(TodoException.class, () -> {
            todoService.deleteTodoById(nonExistentTodoId);  // 투두가 없을 때 예외 발생 검증
        });
    }
    @DisplayName("size=8을 요청하면, 백로그 목록 조회 시 8개의 데이터만 응답된다.")
    @Test
    void 백로그_데이터_8개만_응답(){
        //given
        Long userId = 1L;
        int page = 0;
        int size = 8;
        //when & then
        assertThat(todoService.getBacklogList(userId,page,size).getBacklogs().size()).isEqualTo(size);
    }

    @DisplayName("존재하지 않는 할 일이면 예외가 발생한다.")
    @Test
    void 스와이프_존재하지_않는_할일_예외(){
        //given
        Long notExistTodoId = 1000L;
        Long userId = 1L;
        //when & then
        assertThatThrownBy(()-> todoService.swipe(userId,notExistTodoId))
                .isInstanceOf(TodoException.class)
                .hasMessage(TodoExceptionErrorCode.TODO_NOT_EXIST.getMessage());
    }

    @DisplayName("사용자의 할 일이 아닌 경우 예외가 발생한다.")
    @Test
    void 스와이프_사용자_예외(){
        //given
        Long todoId = 1L;
        Long userId = 50L;
        //when & then
        assertThatThrownBy(()-> todoService.swipe(userId,todoId))
                .isInstanceOf(TodoException.class)
                .hasMessage(TodoExceptionErrorCode.TODO_USER_NOT_MATCH.getMessage());
    }

    @DisplayName("달성한 TODAY이면 예외가 발생한다.")
    @Test
    void 스와이프_달성한_투데이_예외(){
        //given
        Long userId = 1L;
        Long todoId = 3L;
        //when & then
        assertThatThrownBy(()-> todoService.swipe(userId,todoId))
                .isInstanceOf(TodoException.class)
                .hasMessage(TodoExceptionErrorCode.ALREADY_COMPLETED_TODO.getMessage());
    }

    @DisplayName("TODAY인 할일이면 BACKLOG로 수정된다.")
    @Test
    void 스와이프_TODAY에서_BACKLOG로(){
        //given
        Long userId = 1L;
        Long todoId = 4L;
        //when & then
        todoService.swipe(userId,todoId);

        assertThat(todoRepository.findById(todoId).get().getType()).isEqualTo(Type.BACKLOG);
    }

    @DisplayName("BACKLOG인 할일을 스와이프하면 todayOrder가 17이 된다.")
    @Test
    void 스와이프_TODAYORDER_갱신_성공(){
        //given
        Long userId = 1L;
        Long todoId = 18L;
        //when & then
        todoService.swipe(userId,todoId);

        assertThat(todoRepository.findById(todoId).get().getTodayOrder()).isEqualTo(17);
    }

    @Test
    @DisplayName("isBookmark가 true일 때 false로 변경하는 테스트")
    void toggleIsBookmark_TrueToFalse() {
        // given
        Todo todo = Todo.builder()
                .userId(1L)
                .type(Type.TODAY)
                .content("Sample Todo")
                .isBookmark(true)  // isBookmark가 true로 설정됨
                .build();

        // Todo를 실제로 저장
        Todo savedTodo = todoRepository.save(todo);
        Long todoId = savedTodo.getId();

        // when: toggleIsBookmark 호출 (void 메서드)
        todoService.toggleIsBookmark(todoId);

        // then: DB에서 Todo를 다시 가져와서 확인
        Todo updatedTodo = todoRepository.findById(todoId).orElseThrow(() -> new TodoException(TODO_NOT_EXIST));
        assertThat(updatedTodo.isBookmark()).isFalse();  // isBookmark가 false로 변경되었는지 확인
    }

    @Test
    @DisplayName("isBookmark가 false일 때 true로 변경하는 테스트")
    void toggleIsBookmark_FalseToTrue() {
        // given
        Todo todo = Todo.builder()
                .userId(1L)
                .type(Type.TODAY)
                .content("Sample Todo")
                .isBookmark(false)  // isBookmark가 false로 설정됨
                .build();

        // Todo를 실제로 저장
        Todo savedTodo = todoRepository.save(todo);
        Long todoId = savedTodo.getId();

        // when: toggleIsBookmark 호출 (void 메서드)
        todoService.toggleIsBookmark(todoId);

        // then: DB에서 Todo를 다시 가져와서 확인
        Todo updatedTodo = todoRepository.findById(todoId).orElseThrow(() -> new TodoException(TODO_NOT_EXIST));
        assertThat(updatedTodo.isBookmark()).isTrue();  // isBookmark가 true로 변경되었는지 확인
    }

    @Test
    @DisplayName("존재하지 않는 Todo ID일 경우 예외가 발생한다.")
    void toggleIsBookmark_TodoNotFound() {
        // given
        Long nonExistentTodoId = 999L;  // 존재하지 않는 Todo ID

        // when & then
        assertThrows(TodoException.class, () -> {
            todoService.toggleIsBookmark(nonExistentTodoId);  // 예외가 발생해야 함
        });
    }


}