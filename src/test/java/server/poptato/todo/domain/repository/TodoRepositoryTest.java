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
        Pageable pageable = PageRequest.of(0, 8);

        //when
        Page<Todo> result = todoRepository.findByUserIdAndTypeAndTodayDateAndTodayStatusOrderByCompletedDateTimeDesc(
                userId, Type.TODAY, todayDate, TodayStatus.COMPLETED ,pageable);

        assertThat(result).isNotEmpty();
        List<Todo> todos = result.getContent();

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
        Pageable pageable = PageRequest.of(0, 8);

        //when
        Page<Todo> result = todoRepository.findByUserIdAndTypeAndTodayDateAndTodayStatusOrderByTodayOrderAsc(
                userId, Type.TODAY, todayDate, TodayStatus.INCOMPLETE, pageable);

        assertThat(result).isNotEmpty();
        List<Todo> todos = result.getContent();

        // userId가 모두 1인지 확인
        assertThat(todos.stream().allMatch(todo -> todo.getUserId().equals(userId))).isTrue();

        // type이 모두 TODAY인지 확인
        assertThat(todos.stream().allMatch(todo -> todo.getType() == Type.TODAY)).isTrue();

        // todayDate가 모두 2024-10-16인지 확인
        assertThat(todos.stream().allMatch(todo -> todo.getTodayDate().equals(todayDate))).isTrue();

        // 미완료된 할 일은 todayOrder가 오름차순으로 정렬되어야 함
        for (int i = 0; i < todos.size() - 1; i++) {
            assertThat(todos.get(i).getTodayOrder()).isLessThanOrEqualTo(todos.get(i + 1).getTodayOrder());
        }
    }
}