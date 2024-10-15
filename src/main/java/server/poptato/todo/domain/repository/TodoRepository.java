package server.poptato.todo.domain.repository;

import server.poptato.todo.domain.entity.Todo;

import java.util.List;

public interface TodoRepository {
    List<Todo> findAllByUserId(Long userId);
    void deleteAll(List<Todo> todos);
}
