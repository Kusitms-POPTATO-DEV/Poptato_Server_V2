package server.poptato.todo.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import server.poptato.todo.domain.entity.CompletedDateTime;
import server.poptato.todo.domain.repository.CompletedDateTimeRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface JpaCompletedDateTimeRepository extends CompletedDateTimeRepository, JpaRepository<CompletedDateTime,Long> {
    @Query("""
    SELECT c
    FROM CompletedDateTime c 
    WHERE c.todoId = :todoId AND FUNCTION('DATE', c.dateTime) = :todayDate
    """)
    Optional<CompletedDateTime> findByDateAndTodoId(@Param("todoId") Long todoId, @Param("todayDate") LocalDate todayDate);
}
