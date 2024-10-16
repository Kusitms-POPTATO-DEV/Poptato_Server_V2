package server.poptato.todo.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.value.TodayStatus;
import server.poptato.todo.domain.value.Type;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TodoRepository {
    void deleteAllByUserId(Long userId);
    // 미완료된 할 일 조회
    List<Todo> findByUserIdAndTypeAndTodayDateAndTodayStatusOrderByTodayOrderAsc(
            Long userId, Type type, LocalDate todayDate, TodayStatus todayStatus);

    // 완료된 할 일 조회
    List<Todo> findByUserIdAndTypeAndTodayDateAndTodayStatusOrderByCompletedDateTimeDesc(
            Long userId, Type type, LocalDate todayDate, TodayStatus todayStatus);

    Optional<Todo> findById(Long todoId);
    void delete(Todo todo);
     Todo save(Todo todo);
    // 백로그 목록 조회
    Page<Todo> findByUserIdAndTypeInOrderByBacklogOrderAsc(Long userId, List<Type> types, Pageable pageable);
    Page<Todo> findByUserIdAndCompletedDateTimeIsNotNull(Long userId, Pageable pageable);
}
