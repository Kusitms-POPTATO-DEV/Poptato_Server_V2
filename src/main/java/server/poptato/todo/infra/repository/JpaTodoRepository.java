package server.poptato.todo.infra.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.repository.TodoRepository;

import java.util.Optional;

public interface JpaTodoRepository extends TodoRepository, JpaRepository<Todo,Long> {
    @Query("SELECT t FROM Todo t WHERE t.userId = :userId AND t.completedDateTime IS NOT NULL")
    Page<Todo> findByUserIdAndCompletedDateTimeIsNotNull(Long userId, Pageable pageable);
}
