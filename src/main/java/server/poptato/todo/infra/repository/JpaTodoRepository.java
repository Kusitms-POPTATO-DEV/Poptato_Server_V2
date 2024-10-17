package server.poptato.todo.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.repository.TodoRepository;

import java.util.List;
import java.util.Optional;

public interface JpaTodoRepository extends TodoRepository, JpaRepository<Todo,Long> {
    @Query("SELECT COALESCE(MAX(t.backlogOrder), 0) FROM Todo t WHERE t.userId = :userId AND t.backlogOrder IS NOT NULL")
    Integer findMaxBacklogOrderByUserIdOrZero(Long userId);
    @Query("SELECT COALESCE(MAX(t.todayOrder), 0) FROM Todo t WHERE t.userId = :userId AND t.todayOrder IS NOT NULL")
    Integer findMaxTodayOrderByUserIdOrZero(Long userId);
    @Query("SELECT MIN(t.todayOrder) FROM Todo t WHERE t.id IN :ids")
    int findMinTodayOrderByIdIn(@Param("ids") List<Long> ids);
    @Query("SELECT MIN(t.backlogOrder) FROM Todo t WHERE t.id IN :ids")
    int findMinBacklogOrderByIdIn(@Param("ids") List<Long> ids);
}
