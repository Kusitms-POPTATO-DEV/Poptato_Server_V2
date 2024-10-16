package server.poptato.todo.domain.repository;

public interface TodoRepository {
    void deleteAllByUserId(Long userId);
}
