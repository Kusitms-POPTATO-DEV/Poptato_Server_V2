package server.poptato.todo.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.repository.TodoRepository;

import java.util.Optional;

public interface JpaTodoRepository extends TodoRepository, JpaRepository<Todo,Long> {

}
