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

    List<Todo> findCompletedTodayByUserIdOrderByCompletedDateTimeAsc(Long userId, LocalDate todayDate);

    Optional<Todo> findById(Long todoId);

    void delete(Todo todo);

    Page<Todo> findByUserIdAndCompletedStatus(Long userId, TodayStatus todayStatus, Pageable pageable);

    Todo save(Todo todo);

    Page<Todo> findBacklogsByUserId(Long userId, List<Type> types, List<TodayStatus> statuses, Pageable pageable);

    Integer findMaxBacklogOrderByUserIdOrZero(Long userId);

    Integer findMaxTodayOrderByUserIdOrZero(Long userId);

    Integer findMinTodayOrderByUserIdOrZero(Long userId);

    int findMaxBacklogOrderByIdIn(List<Long> ids);

    int findMaxTodayOrderByIdIn(List<Long> ids);

    Page<Todo> findByUserIdAndTypeAndTodayStatus(Long userId, Type type, TodayStatus todayStatus, Pageable pageable);

    List<Todo> findByTypeAndTodayStatus(Type today, TodayStatus incomplete);

    Integer findMinBacklogOrderByUserIdOrZero(Long userId);

    default List<Todo> findIncompleteTodays(Long userId, Type type, LocalDate todayDate, TodayStatus todayStatus) {
        return findByUserIdAndTypeAndTodayDateAndTodayStatusOrderByTodayOrderDesc(
                userId, type, todayDate, todayStatus);
    }

    default List<Todo> findCompletedTodays(Long userId, LocalDate todayDate) {
        return findCompletedTodayByUserIdOrderByCompletedDateTimeAsc(
                userId, todayDate);
    }

    default Page<Todo> findHistories(Long userId,Pageable pageable) {
        return findByUserIdAndCompletedStatus(userId, TodayStatus.COMPLETED, pageable);
    }

    List<Todo> findByType(Type type);
    List<Todo> findByTypeAndUserId(Type type, Long userId);
}
