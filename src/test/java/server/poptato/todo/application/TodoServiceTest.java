package server.poptato.todo.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import server.poptato.todo.api.request.DragAndDropRequestDto;
import server.poptato.todo.application.response.*;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.repository.TodoRepository;
import server.poptato.todo.domain.value.TodayStatus;
import server.poptato.todo.domain.value.Type;
import server.poptato.todo.exception.TodoException;
import server.poptato.todo.exception.errorcode.TodoExceptionErrorCode;
import server.poptato.user.exception.UserException;
import server.poptato.user.exception.errorcode.UserExceptionErrorCode;

import java.time.LocalDate;
import java.util.List;
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
        LocalDate todayDate = LocalDate.now();
        //when & then
        assertThatThrownBy(()-> todoService.getTodayList(notExistUserId,page,size,todayDate))
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
        LocalDate todayDate = LocalDate.of(2024,10,16);
        //when & then
        assertThat(todoService.getTodayList(userId,page,size,todayDate).getTodays().size()).isEqualTo(size);
    }

    @DisplayName("유효하지 않는 페이지 수일 경우 빈 리스트를 반환한다.")
    @Test
    void 유효하지_않은_페이지_수_응답(){
        //given
        Long userId = 1L;
        int page = 2;
        int size = 8;
        LocalDate todayDate = LocalDate.of(2024,10,16);
        //when
        TodayListResponseDto todayList = todoService.getTodayList(userId, page, size, todayDate);
        //then
        assertThat(todayList.getTodays()).isEmpty();
    }

    @DisplayName("투데이_데이터가 없는 경우 빈 리스트를 반환한다.")
    @Test
    void 투데이_데이터_수_0개_응답(){
        //given
        Long userId = 50L;
        int page = 0;
        int size = 8;
        LocalDate todayDate = LocalDate.now();
        //when 
        TodayListResponseDto todayList = todoService.getTodayList(userId, page, size, todayDate);
        //then
        assertThat(todayList.getTodays()).isEmpty();
        assertThat(todayList.getTotalPageCount()).isEqualTo(0);
    }

    @DisplayName("백로그_데이터가 없는 경우 빈 리스트를 반환한다.")
    @Test
    void 백로그_데이터_수_0개_응답(){
        //given
        Long userId = 50L;
        int page = 0;
        int size = 8;
        //when
        BacklogListResponseDto backlogList = todoService.getBacklogList(userId, page, size);
        //then
        assertThat(backlogList.getBacklogs()).isEmpty();
        assertThat(backlogList.getTotalPageCount()).isEqualTo(0);
    }

    @DisplayName("투데이 목록 조회 시, 미달성 투데이가 먼저 조회되고, 그 다음 달성 투데이가 조회된다.")
    @Test
    void 투데이_목록_조회_달성여부_정렬(){
        //given
        Long userId = 1L;
        int page = 0;
        int size = 8;
        LocalDate todayDate = LocalDate.now();

        //when
        TodayListResponseDto todayList = todoService.getTodayList(userId, page, size, todayDate);

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
        Long nonExistentTodoId = 1000L;  // 존재하지 않는 투두 ID

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
    @DisplayName("드래그앤드롭 시 사용자의 할일이 아니면 예외가 발생한다.")
    @Test
    void 드래그앤드롭_사용자의_할일이_아닌_예외(){
        //given
        Long userId = 50L;
        DragAndDropRequestDto request = DragAndDropRequestDto.builder()
                .type(Type.TODAY)
                .todoIds(List.of(1L))
                .build();

        //when & then
        assertThatThrownBy(()-> todoService.dragAndDrop(userId,request))
                .isInstanceOf(TodoException.class)
                .hasMessage(TodoExceptionErrorCode.TODO_USER_NOT_MATCH.getMessage());
    }

    @DisplayName("드래그앤드롭 시 할 일이 Type과 맞지 않으면 예외가 발생한다.")
    @Test
    void 드래그앤드롭_타입이_맞지_않는_예외(){
        //given
        Long userId = 1L;
        DragAndDropRequestDto request = DragAndDropRequestDto.builder()
                .type(Type.BACKLOG)
                .todoIds(List.of(1L))
                .build();
        //when & then
        assertThatThrownBy(()-> todoService.dragAndDrop(userId,request))
                .isInstanceOf(TodoException.class)
                .hasMessage(TodoExceptionErrorCode.TODO_TYPE_NOT_MATCH.getMessage());
    }

    @DisplayName("드래그앤드롭 시 이미 달성한 TODAY 포함 시 예외가 발생한다.")
    @Test
    void 드래그앤드롭_이미_달성한_투데이_예외(){
        //given
        Long userId = 1L;
        DragAndDropRequestDto request = DragAndDropRequestDto.builder()
                .type(Type.TODAY)
                .todoIds(List.of(3L))
                .build();
        //when & then
        assertThatThrownBy(()-> todoService.dragAndDrop(userId,request))
                .isInstanceOf(TodoException.class)
                .hasMessage(TodoExceptionErrorCode.ALREADY_COMPLETED_TODO.getMessage());
    }

    @DisplayName("드래그앤드롭 시 할 일들의 Order를 재정렬한다.")
    @Test
    void 드래그앤드롭_순서_재정렬(){
        //given
        Long userId = 1L;
        DragAndDropRequestDto request = DragAndDropRequestDto.builder()
                .type(Type.TODAY)
                .todoIds(List.of(1L,5L,2L,4L))
                .build();
        //when
        todoService.dragAndDrop(userId,request);
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
    @Test
    @DisplayName("Histories 페이징 및 정렬 테스트")
    void getHistories_ShouldReturnPagedAndSortedResult() {
        // given
        Long userId = 1L;
        int page = 0;
        int size = 5;

        // when
        PaginatedHistoryResponseDto result = todoService.getHistories(userId, page, size);

        // then
        // result의 histories 리스트의 실제 크기
        int actualSize = result.getHistories().size();

        // 페이지당 반환되는 항목의 수가 size 이하인지 확인 (마지막 페이지일 경우 적을 수 있음)
        assertThat(actualSize).isLessThanOrEqualTo(size);

        // 전체 페이지 수가 적절하게 계산되었는지 확인
        assertThat(result.getTotalPageCount()).isGreaterThan(0);

        // 내림차순 정렬이 올바르게 되었는지 확인
        List<HistoryResponseDto> histories = result.getHistories();
        for (int i = 0; i < histories.size() - 1; i++) {
            LocalDate current = histories.get(i).getDate();
            LocalDate next = histories.get(i + 1).getDate();

            // 현재 항목의 날짜가 다음 항목의 날짜보다 같거나 이후인지(내림차순인지) 확인
            assertThat(current).isAfterOrEqualTo(next);
        }
    }


    @Test
    @DisplayName("YESTERDAY 타입과 INCOMPLETE 상태인 todo 목록을 페이징 처리하여 가져온다")
    void getYesterdays_ShouldReturnPagedResult() {
        // given
        Long userId = 1L;
        int page = 0;
        int size = 5; // 한번에 5개의 todo 가져오기

        // when
        PaginatedYesterdayResponseDto result = todoService.getYesterdays(userId, page, size);

        // then
        assertThat(result.getYesterdays()).hasSizeLessThanOrEqualTo(size); // 페이징 크기만큼 가져옴
        assertThat(result.getTotalPageCount()).isGreaterThan(0);  // 총 페이지 수가 0보다 큼
        assertThat(result.getYesterdays().get(0).getTodoId()).isNotNull(); // 첫 번째 todo의 ID가 null이 아님
        assertThat(result.getYesterdays().get(0).getContent()).isNotNull(); // 첫 번째 todo의 내용이 null이 아님
    }

    @DisplayName("할 일 상세조회 시 성공한다.")
    @Test
    void 할일_상세조회_성공(){
        //given
        Long userId = 1L;
        Long todoId = 10L;
        //when
        TodoDetailResponseDto todoInfo = todoService.getTodoInfo(userId, todoId);
        //then
        assertThat(todoInfo.getContent()).isEqualTo("할 일 10");
        assertThat(todoInfo.getDeadline()).isEqualTo(LocalDate.of(2024,10,26));
        assertThat(todoInfo.getIsBookmark()).isTrue();
    }

    @DisplayName("마감기한 수정 시 성공한다.")
    @Test
    void 마감기한_수정_성공(){
        //given
        Long userId = 1L;
        Long todoId = 11L;
        LocalDate updateDate = LocalDate.of(2024,12,25);

        //when
        todoService.updateDeadline(userId,todoId,updateDate);
        Todo findTodo = todoRepository.findById(todoId).get();

        //then
        assertThat(findTodo.getDeadline()).isEqualTo(updateDate);
    }

    @DisplayName("할일 내용 수정 시 성공한다.")
    @Test
    void 할일_내용_수정_성공(){
        //given
        Long userId = 1L;
        Long todoId = 11L;
        String updateContent = "할일 내용 수정";

        //when
        todoService.updateContent(userId,todoId,updateContent);
        Todo findTodo = todoRepository.findById(todoId).get();

        //then
        assertThat(findTodo.getContent()).isEqualTo(updateContent);
    }

    @DisplayName("투데이 달성여부 변경 시 성공한다.")
    @Test
    void 투데이_달성여부_변경_성공(){
        //given
        Long userId = 1L;
        Long todoId = 1L;

        //when
        todoService.updateIsCompleted(userId,todoId);
        Todo findTodo = todoRepository.findById(todoId).get();

        //then
        assertThat(findTodo.getTodayStatus()).isEqualTo(TodayStatus.COMPLETED);
        assertThat(findTodo.getCompletedDateTime()).isNotNull();
    }

    @DisplayName("어제한일 달성여부 변경 시 성공한다.")
    @Test
    void 어제한일_달성여부_변경_성공(){
        //given
        Long userId = 1L;
        Long todoId = 35L;

        //when
        todoService.updateIsCompleted(userId,todoId);
        Todo findTodo = todoRepository.findById(todoId).get();

        //then
        assertThat(findTodo.getTodayStatus()).isEqualTo(TodayStatus.COMPLETED);
        assertThat(findTodo.getCompletedDateTime()).isNotNull();
    }

    @DisplayName("백로그 달성여부 변경 시 예외가 발생한다.")
    @Test
    void 백로그_달성여부_변경_예외(){
        //given
        Long userId = 1L;
        Long todoId = 20L;

        //when & then
        assertThatThrownBy(()-> todoService.updateIsCompleted(userId,todoId))
                .isInstanceOf(TodoException.class)
                .hasMessage(TodoExceptionErrorCode.BACKLOG_CANT_COMPLETE.getMessage());
    }
}