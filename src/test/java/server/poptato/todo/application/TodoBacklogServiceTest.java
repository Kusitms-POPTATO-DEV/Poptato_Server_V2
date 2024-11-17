package server.poptato.todo.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import server.poptato.todo.api.request.BacklogCreateRequestDto;
import server.poptato.todo.application.response.*;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.repository.TodoRepository;
import server.poptato.todo.domain.value.Type;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TodoBacklogServiceTest {
    @Autowired
    private TodoBacklogService todoBacklogService;
    @Autowired
    private TodoRepository todoRepository;

    @DisplayName("백로그 목록 조회 시, size=8을 요청하면 8개가 응답된다.")
    @Test
    void getBackList_Success() {
        //given
        Long userId = 1L;
        int page = 0;
        int size = 8;

        //when
        List<BacklogResponseDto> backlogList = todoBacklogService.getBacklogList(userId, page, size).getBacklogs();

        //then
        assertThat(backlogList.size()).isEqualTo(size);
    }

    @DisplayName("백로그 목록 조회 시, 백로그 데이터가 없는 경우 빈 리스트를 반환한다.")
    @Test
    void getBacklogList_EmptyToday_Success() {
        //given
        Long userId = 50L;
        int page = 0;
        int size = 8;

        //when
        BacklogListResponseDto backlogList = todoBacklogService.getBacklogList(userId, page, size);
        for(BacklogResponseDto todo : backlogList.getBacklogs()){
            Long todoId = todo.getTodoId();
            System.out.println(todoId);
        }

        //then
        assertThat(backlogList.getBacklogs().size()).isEqualTo(0);
        assertThat(backlogList.getTotalPageCount()).isEqualTo(0);
    }

    @DisplayName("백로그 생성 시 성공한다.")
    @Test
    void generateBacklog_Success() {
        //given
        Long userId = 1L;
        String content = "할 일 내용";
        BacklogCreateRequestDto backlogCreateRequestDto = BacklogCreateRequestDto.builder()
                .content(content)
                .build();
        Integer maxBacklogOrder = todoRepository.findMaxBacklogOrderByUserIdOrZero(userId);

        //when
        BacklogCreateResponseDto backlogCreateResponseDto = todoBacklogService.generateBacklog(userId, backlogCreateRequestDto);
        Todo newTodo = todoRepository.findById(backlogCreateResponseDto.todoId()).get();

        //then
        assertThat(newTodo).isNotNull();
        assertThat(newTodo.getUserId()).isEqualTo(userId);
        assertThat(newTodo.getContent()).isEqualTo(content);
        assertThat(newTodo.getBacklogOrder()).isEqualTo(maxBacklogOrder + 1);
        assertThat(newTodo.getType()).isEqualTo(Type.BACKLOG);
        assertThat(newTodo.isBookmark()).isFalse();
        assertThat(newTodo.getTodayStatus()).isNull();
    }
    @Test
    @DisplayName("기록 조회 시 페이징 및 정렬하여 기록 조회를 성공한다.")
    void getHistories_Success() {
        // given
        Long userId = 1L;
        int page = 0;
        int size = 5;

        // when
        PaginatedHistoryResponseDto historiesPage = todoBacklogService.getHistories(userId, page, size);

        // then
        int actualSize = historiesPage.getHistories().size();

        assertThat(actualSize).isLessThanOrEqualTo(size);
        assertThat(historiesPage.getTotalPageCount()).isGreaterThan(0);

    }


    @Test
    @DisplayName("어제한일 조회 시 성공한다.")
    void getYesterdays_Success() {
        // given
        Long userId = 1L;
        int page = 0;
        int size = 5;
        // when
        PaginatedYesterdayResponseDto result = todoBacklogService.getYesterdays(userId, page, size);
        Long todoId = result.getYesterdays().get(0).getTodoId();
        Optional<Todo> todo = todoRepository.findById(todoId);

        // then
        assertThat(result.getYesterdays()).hasSizeLessThanOrEqualTo(size);
        assertThat(result.getTotalPageCount()).isGreaterThan(0);
        assertThat(result.getYesterdays().get(0).getTodoId()).isNotNull();
        assertThat(result.getYesterdays().get(0).getContent()).isNotNull();
        assertThat(result.getYesterdays().get(0).isBookmark()).isNotNull();
        assertThat(result.getYesterdays().get(0).getDDay()).isEqualTo((int) ChronoUnit.DAYS.between(LocalDate.now(), todo.get().getDeadline()));
    }
}