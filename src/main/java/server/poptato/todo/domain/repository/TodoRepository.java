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
    List<Todo> findByUserIdAndTypeAndTodayDateAndTodayStatusOrderByCompletedDateTimeDesc(
            Long userId, Type type, LocalDate todayDate, TodayStatus todayStatus);
    Optional<Todo> findById(Long todoId);
    Optional<Todo> findByIdAndUserId(Long todoId, Long userId);
    void delete(Todo todo);
    Page<Todo> findByUserIdAndCompletedDateTimeIsNotNullAndTypeIn(Long userId, List<Type> types, Pageable pageable);
    Todo save(Todo todo);
    Page<Todo> findByUserIdAndTypeInOrderByBacklogOrderDesc(Long userId, List<Type> types, Pageable pageable);
    Integer findMaxBacklogOrderByUserIdOrZero(Long userId);
    Integer findMaxTodayOrderByUserIdOrZero(Long userId);
    Integer findMinTodayOrderByUserIdOrZero(Long userId);
    int findMaxBacklogOrderByIdIn(List<Long> ids);
    int findMaxTodayOrderByIdIn(List<Long> ids);
    Page<Todo> findByUserIdAndTypeAndTodayStatus(Long userId, Type type, TodayStatus todayStatus, Pageable pageable);
    List<Todo> findByTypeAndTodayStatus(Type today, TodayStatus incomplete);
    Integer findMinBacklogOrderByUserIdOrZero(Long userId);

    default List<Todo> findIncompleteTodays(Long userId, Type type, LocalDate todayDate, TodayStatus todayStatus){
        return findByUserIdAndTypeAndTodayDateAndTodayStatusOrderByTodayOrderDesc(
                userId, type, todayDate, todayStatus);
    }
    default List<Todo> findCompletedTodays(Long userId, Type type, LocalDate todayDate, TodayStatus todayStatus){
        return findByUserIdAndTypeAndTodayDateAndTodayStatusOrderByCompletedDateTimeDesc(
                userId, type, todayDate, todayStatus);
    }

    default Page<Todo> findBacklogsByUserId(Long userId, List<Type> types, Pageable pageable){
        return findByUserIdAndTypeInOrderByBacklogOrderDesc(
                userId, types, pageable);
    }
    default Page<Todo> findHistories(Long userId, List<Type> types, Pageable pageable) {
        return findByUserIdAndTypeInOrderByBacklogOrderDesc(userId, types, pageable);
    }
}
