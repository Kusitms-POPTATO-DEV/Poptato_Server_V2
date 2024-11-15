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

    void delete(Todo todo);

    Page<Todo> findByUserIdAndCompletedStatusAndDifferentTodayDate(Long userId, TodayStatus todayStatus, LocalDate today, Pageable pageable);

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

    default List<Todo> findCompletedTodays(Long userId, Type type, LocalDate todayDate, TodayStatus todayStatus) {
        return findByUserIdAndTypeAndTodayDateAndTodayStatusOrderByCompletedDateTimeAsc(
                userId, type, todayDate, todayStatus);
    }

    default Page<Todo> findHistories(Long userId, LocalDate today, Pageable pageable) {
        return findByUserIdAndCompletedStatusAndDifferentTodayDate(userId, TodayStatus.COMPLETED, today, pageable);
    }

    List<Todo> findByType(Type type);
    List<Todo> findByTypeAndUserId(Type type, Long userId);
    List<LocalDate> findCalendarDatesByYearAndMonth(int year, int month);
}
