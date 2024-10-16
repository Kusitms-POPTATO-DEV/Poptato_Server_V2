package server.poptato.todo.application.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.repository.TodoRepository;

@Service
@RequiredArgsConstructor
public class TodoService {
    private final TodoRepository todoRepository;

    @Transactional
    public void deleteTodoById(Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new EntityNotFoundException("Todo not found with id: " + todoId));
        todoRepository.delete(todo);
    }
}
