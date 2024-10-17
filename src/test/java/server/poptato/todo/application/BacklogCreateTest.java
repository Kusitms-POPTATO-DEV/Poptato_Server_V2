package server.poptato.todo.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.repository.TodoRepository;
import server.poptato.todo.domain.value.Type;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class BacklogCreateTest {
    @Autowired
    private TodoService todoService;
    @Autowired
    private TodoRepository todoRepository;

    @DisplayName("백로그 생성 시 할 일의 값들이 기본값으로 설정된다.")
    @Test
    void 백로그_생성_속성_기본값(){
        //given
        Long userId = 1L;
        String content = "내용";

        //when
        Long todoId = todoService.generateBacklog(userId,content);
        Todo findTodo = todoRepository.findById(todoId).get();

        //then
        assertThat(findTodo.getId()).isNotNull();
        assertThat(findTodo.getTodayOrder()).isNull();
        assertThat(findTodo.getTodayDate()).isNull();
        assertThat(findTodo.getTodayStatus()).isNull();
        assertThat(findTodo.getDeadline()).isNull();
        assertThat(findTodo.getContent()).isEqualTo(content);
        assertThat(findTodo.getType()).isEqualTo(Type.BACKLOG);
        assertThat(findTodo.getUserId()).isEqualTo(userId);
        assertThat(findTodo.isBookmark()).isEqualTo(false);
        assertThat(findTodo.getBacklogOrder()).isEqualTo(21L);
        assertThat(findTodo.getCompletedDateTime()).isNull();
    }
}
