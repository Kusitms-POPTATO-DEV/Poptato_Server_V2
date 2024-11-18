package server.poptato.todo.infra.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.repository.TodoRepository;
import server.poptato.todo.domain.value.TodayStatus;
import server.poptato.todo.domain.value.Type;

import java.time.LocalDate;
import java.util.List;

public interface JpaTodoRepository extends TodoRepository, JpaRepository<Todo, Long> {
    @Query("SELECT COALESCE(MAX(t.backlogOrder), 0) FROM Todo t WHERE t.userId = :userId AND t.backlogOrder IS NOT NULL")
    Integer findMaxBacklogOrderByUserIdOrZero(Long userId);

    @Query("SELECT COALESCE(MAX(t.todayOrder), 0) FROM Todo t WHERE t.userId = :userId AND t.todayOrder IS NOT NULL")
    Integer findMaxTodayOrderByUserIdOrZero(Long userId);

    @Query("SELECT MAX(t.todayOrder) FROM Todo t WHERE t.id IN :ids")
    int findMaxTodayOrderByIdIn(@Param("ids") List<Long> ids);

    @Query("SELECT MAX(t.backlogOrder) FROM Todo t WHERE t.id IN :ids")
    int findMaxBacklogOrderByIdIn(@Param("ids") List<Long> ids);

    @Query("SELECT COALESCE(MIN(t.backlogOrder), 0) FROM Todo t WHERE t.userId = :userId AND t.backlogOrder IS NOT NULL")
    Integer findMinBacklogOrderByUserIdOrZero(@Param("userId") Long userId);

    @Query("SELECT COALESCE(MIN(t.todayOrder), 0) FROM Todo t WHERE t.userId = :userId AND t.todayOrder IS NOT NULL")
    Integer findMinTodayOrderByUserIdOrZero(Long userId);

    @Query("SELECT t FROM Todo t WHERE t.userId = :userId AND t.todayStatus = :todayStatus")
    Page<Todo> findByUserIdAndCompletedStatus(
            @Param("userId") Long userId,
            @Param("todayStatus") TodayStatus todayStatus,
            Pageable pageable
    );

    @Query("SELECT t FROM Todo t WHERE t.userId = :userId AND (t.type IN :types AND (t.todayStatus NOT IN :statuses OR t.todayStatus IS NULL)) " +
            "ORDER BY t.backlogOrder DESC")
    Page<Todo> findAllBacklogs(
            @Param("userId") Long userId,
            @Param("types") List<Type> types,
            @Param("statuses") List<TodayStatus> statuses,
            Pageable pageable);

    @Query("SELECT t FROM Todo t WHERE t.userId = :userId AND t.isBookmark = true AND t.type IN :types AND (t.todayStatus NOT IN :statuses OR t.todayStatus IS NULL) ORDER BY t.backlogOrder DESC")
    Page<Todo> findBookmarkBacklogs(
            @Param("userId") Long userId,
            @Param("types") List<Type> types,
            @Param("statuses") List<TodayStatus> statuses,
            Pageable pageable);

    @Query("SELECT t FROM Todo t WHERE t.userId = :userId AND t.categoryId = :categoryId AND " +
            "(t.type IN :types AND (t.todayStatus NOT IN :statuses OR t.todayStatus IS NULL)) " +
            "ORDER BY t.backlogOrder DESC")
    Page<Todo> findBacklogsByCategoryId(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("types") List<Type> types,
            @Param("statuses") List<TodayStatus> statuses,
            Pageable pageable);

    @Query("""
            SELECT t 
            FROM Todo t
            JOIN CompletedDateTime c ON t.id = c.todoId
            WHERE t.userId = :userId 
              AND t.type = 'TODAY'
              AND t.todayStatus = 'COMPLETED'
              AND FUNCTION('DATE', c.dateTime) = :todayDate
            ORDER BY c.dateTime ASC
            """)
    List<Todo> findCompletedTodayByUserIdOrderByCompletedDateTimeAsc(
            @Param("userId") Long userId,
            @Param("todayDate") LocalDate todayDate
    );

    @Query("SELECT t FROM Todo t " +
            "WHERE t.id IN (" +
            "    SELECT c.todoId FROM CompletedDateTime c " +
            "    WHERE DATE(c.dateTime) = :localDate" +
            ") AND t.userId = :userId " +
            "ORDER BY (SELECT c.dateTime FROM CompletedDateTime c WHERE c.todoId = t.id) ASC")
    Page<Todo> findTodosByUserIdAndCompletedDateTime(@Param("userId") Long userId,
                                                     @Param("localDate") LocalDate localDate, Pageable pageable);
}
