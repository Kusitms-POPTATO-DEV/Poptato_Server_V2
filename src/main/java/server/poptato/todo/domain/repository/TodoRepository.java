package server.poptato.todo.domain.repository;

import server.poptato.todo.domain.entity.Todo;

import java.util.Optional;

public interface TodoRepository {
    void deleteAllByUserId(Long userId);
    Optional<Todo> findById(Long todoId);
    void delete(Todo todo);
}
