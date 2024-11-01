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
    List<Todo> findByUserIdAndTypeAndTodayDateAndTodayStatusOrderByTodayOrderDesc(
            Long userId, Type type, LocalDate todayDate, TodayStatus todayStatus);
    List<Todo> findByUserIdAndTypeAndTodayDateAndTodayStatusOrderByCompletedDateTimeAsc(
            Long userId, Type type, LocalDate todayDate, TodayStatus todayStatus);
    Optional<Todo> findById(Long todoId);
    Optional<Todo> findByIdAndUserId(Long todoId, Long userId);
    void delete(Todo todo);
    Page<Todo> findByUserIdAndCompletedDateTimeIsNotNull(Long userId, Pageable pageable);
    Todo save(Todo todo);
    Page<Todo> findByUserIdAndTypeInOrderByBacklogOrderDesc(Long userId, List<Type> types, Pageable pageable);
    Integer findMaxBacklogOrderByUserIdOrZero(Long userId);
    Integer findMaxTodayOrderByUserIdOrZero(Long userId);
    int findMaxBacklogOrderByIdIn(List<Long> ids);
    int findMaxTodayOrderByIdIn(List<Long> ids);
    Page<Todo> findByUserIdAndTypeAndTodayStatus(Long userId, Type type, TodayStatus todayStatus, Pageable pageable);
    List<Todo> findByTypeAndTodayStatus(Type today, TodayStatus incomplete);
    Integer findMinBacklogOrderByUserIdOrZero(Long userId);
}
