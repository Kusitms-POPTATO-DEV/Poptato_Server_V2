package server.poptato.todo.application;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import server.poptato.todo.application.service.TodoService;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.repository.TodoRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private TodoService todoService;

    @Test
    public void shouldDeleteTodoById_WhenTodoExists() {
        Long todoId = 1L;
        Todo todo = new Todo();
        todo.setId(todoId);

        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));
        
        todoService.deleteTodoById(todoId);

        // then
        verify(todoRepository, times(1)).delete(todo);
    }

    @Test
    public void shouldThrowException_WhenTodoNotFound() {
        // given
        Long todoId = 1L;
        when(todoRepository.findById(todoId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class, () -> {
            todoService.deleteTodoById(todoId);
        });
    }
}
