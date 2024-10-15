package server.poptato.todo.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.repository.TodoRepository;

import java.util.List;

public interface JpaTodoRepository extends TodoRepository, JpaRepository<Todo,Long> {
    @Override
    List<Todo> findAllByUserId(Long userId);
    @Override
    void deleteAll(List<Todo> todos);

}
