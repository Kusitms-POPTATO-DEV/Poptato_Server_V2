package server.poptato.todo.application;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import server.poptato.todo.application.service.TodoService;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.repository.TodoRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
public class TodoServiceTest {

    @MockBean
    private TodoRepository todoRepository;

    @Autowired
    private TodoService todoService;

    @Test
    public void shouldDeleteTodoById_WhenTodoExists() {
        Long todoId = 1L;
        Todo todo = Todo.builder()
                .id(todoId)
                .build();

        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));

        todoService.deleteTodoById(todoId);

        verify(todoRepository, times(1)).delete(todo);
    }

    @Test
    public void shouldThrowException_WhenTodoNotFound() {
        Long todoId = 1L;
        when(todoRepository.findById(todoId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            todoService.deleteTodoById(todoId);
        });
    }
}
