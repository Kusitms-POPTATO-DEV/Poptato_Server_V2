package server.poptato.todo.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.value.TodayStatus;
import server.poptato.todo.domain.value.Type;

import java.time.LocalDate;

public interface TodoRepository {
    void deleteAllByUserId(Long userId);
    // 미완료된 할 일 조회
    Page<Todo> findByUserIdAndTypeAndTodayDateAndTodayStatusOrderByTodayOrderAsc(
            Long userId, Type type, LocalDate todayDate, TodayStatus todayStatus, Pageable pageable);

    // 완료된 할 일 조회
    Page<Todo> findByUserIdAndTypeAndTodayDateAndTodayStatusOrderByCompletedDateTimeDesc(
            Long userId, Type type, LocalDate todayDate, TodayStatus todayStatus, Pageable pageable);

}
