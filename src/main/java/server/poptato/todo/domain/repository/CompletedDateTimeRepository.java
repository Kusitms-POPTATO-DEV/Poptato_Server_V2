package server.poptato.todo.domain.repository;

import server.poptato.todo.domain.entity.CompletedDateTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface CompletedDateTimeRepository {
    Optional<CompletedDateTime> findByDateAndTodoId(Long id, LocalDate todayDate);
    boolean existsByDateTimeAndTodoId(LocalDateTime dateTime, Long todoId);
    void delete(CompletedDateTime completedDateTime);
    CompletedDateTime save(CompletedDateTime completedDateTime);
}
