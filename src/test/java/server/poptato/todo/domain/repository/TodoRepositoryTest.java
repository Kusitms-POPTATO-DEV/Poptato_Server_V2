package server.poptato.todo.domain.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.value.TodayStatus;
import server.poptato.todo.domain.value.Type;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
class TodoRepositoryTest {
    @Autowired
    private TodoRepository todoRepository;

    @DisplayName("userId가 1이 등록한 달성된 투데이가 달성 시각 순서에 따라 성공적으로 정렬되어 조회된다.")
    @Test
    void 투데이_달성_목록조회_성공응답() {
        //given
        Long userId = 1L;
        LocalDate todayDate = LocalDate.of(2024, 10, 16);

        //when
        List<Todo> todos = todoRepository.findByUserIdAndTypeAndTodayDateAndTodayStatusOrderByCompletedDateTimeDesc(
                userId, Type.TODAY, todayDate, TodayStatus.COMPLETED);

        assertThat(todos).isNotEmpty();

        // userId가 모두 1인지 확인
        assertThat(todos.stream().allMatch(todo -> todo.getUserId().equals(userId))).isTrue();

        // type이 모두 TODAY인지 확인
        assertThat(todos.stream().allMatch(todo -> todo.getType() == Type.TODAY)).isTrue();

        // todayDate가 모두 2024-10-16인지 확인
        assertThat(todos.stream().allMatch(todo -> todo.getTodayDate().equals(todayDate))).isTrue();

        // 완료된 할 일은 completeDateTime이 내림차순으로 정렬되어야 함
        for (int i = 0; i < todos.size() - 1; i++) {
            assertThat(todos.get(i).getCompletedDateTime()).isAfterOrEqualTo(todos.get(i + 1).getCompletedDateTime());
        }
    }

    @DisplayName("userId가 1이 등록한 미달성 투데이가 순서에 따라 성공적으로 정렬되어 조회된다.")
    @Test
    void 투데이_미달성_목록조회_성공응답() {
        //given
        Long userId = 1L;
        LocalDate todayDate = LocalDate.of(2024, 10, 16);

        //when
        List<Todo> todos = todoRepository.findByUserIdAndTypeAndTodayDateAndTodayStatusOrderByTodayOrderDesc(
                userId, Type.TODAY, todayDate, TodayStatus.INCOMPLETE);

        assertThat(todos).isNotEmpty();

        // userId가 모두 1인지 확인
        assertThat(todos.stream().allMatch(todo -> todo.getUserId().equals(userId))).isTrue();

        // type이 모두 TODAY인지 확인
        assertThat(todos.stream().allMatch(todo -> todo.getType() == Type.TODAY)).isTrue();

        // todayDate가 모두 2024-10-16인지 확인
        assertThat(todos.stream().allMatch(todo -> todo.getTodayDate().equals(todayDate))).isTrue();

        // 미완료된 할 일은 todayOrder가 오름차순으로 정렬되어야 함
        for (int i = 0; i < todos.size() - 1; i++) {
            assertThat(todos.get(i).getTodayOrder()).isGreaterThan(todos.get(i + 1).getTodayOrder());
        }
    }

    @DisplayName("userId가 1이 등록한 백로그 리스트가 순서에 따라 성공적으로 정렬되어 조회된다.")
    @Test
    void 백로그_목록조회_성공응답() {
        //given
        Long userId = 1L;
        List<Type> types = List.of(Type.BACKLOG, Type.YESTERDAY);
        PageRequest pageRequest = PageRequest.of(0, 8);

        //when
        Page<Todo> backlogs = todoRepository.findByUserIdAndTypeInOrderByBacklogOrderDesc(
                userId, types, pageRequest);

        assertThat(backlogs.getContent()).isNotEmpty();

        // userId가 모두 1인지 확인
        assertThat(backlogs.getContent().stream().allMatch(backlog -> backlog.getUserId().equals(userId))).isTrue();

        // type이 BACKLOG 혹은 YESTERDAY인지 확인
        assertThat(backlogs.getContent().stream().allMatch(backlog -> backlog.getType().equals(Type.BACKLOG)  || backlog.getType().equals(Type.YESTERDAY))).isTrue();

        // backlogOrder가 오름차순으로 정렬되어야 함
        for (int i = 0; i < backlogs.getContent().size() - 1; i++) {
            assertThat(backlogs.getContent().get(i).getBacklogOrder()).isGreaterThan(backlogs.getContent().get(i + 1).getBacklogOrder());
        }
    }
}