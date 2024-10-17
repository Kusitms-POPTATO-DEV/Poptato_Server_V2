package server.poptato.todo.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import server.poptato.todo.api.request.DragAndDropRequestDto;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.repository.TodoRepository;
import server.poptato.todo.domain.value.Type;
import server.poptato.todo.exception.TodoException;
import server.poptato.todo.exception.errorcode.TodoExceptionErrorCode;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class TodoServiceDragAndDropTest {
    @Autowired
    private TodoService todoService;
    @Autowired
    private TodoRepository todoRepository;

    @DisplayName("드래그앤드롭 시 사용자의 할일이 아니면 예외가 발생한다.")
    @Test
    void 드래그앤드롭_사용자의_할일이_아닌_예외(){
        //given
        Long userId = 50L;
        DragAndDropRequestDto request = DragAndDropRequestDto.builder()
                .type(Type.TODAY)
                .todoIds(List.of(1L))
                .build();

        //when & then
        assertThatThrownBy(()-> todoService.dragAndDrop(userId,request))
                .isInstanceOf(TodoException.class)
                .hasMessage(TodoExceptionErrorCode.TODO_USER_NOT_MATCH.getMessage());
    }

    @DisplayName("드래그앤드롭 시 할 일이 Type과 맞지 않으면 예외가 발생한다.")
    @Test
    void 드래그앤드롭_타입이_맞지_않는_예외(){
        //given
        Long userId = 1L;
        DragAndDropRequestDto request = DragAndDropRequestDto.builder()
                .type(Type.BACKLOG)
                .todoIds(List.of(1L))
                .build();
        //when & then
        assertThatThrownBy(()-> todoService.dragAndDrop(userId,request))
                .isInstanceOf(TodoException.class)
                .hasMessage(TodoExceptionErrorCode.TODO_TYPE_NOT_MATCH.getMessage());
    }

    @DisplayName("드래그앤드롭 시 이미 달성한 TODAY 포함 시 예외가 발생한다.")
    @Test
    void 드래그앤드롭_이미_달성한_투데이_예외(){
        //given
        Long userId = 1L;
        DragAndDropRequestDto request = DragAndDropRequestDto.builder()
                .type(Type.TODAY)
                .todoIds(List.of(3L))
                .build();
        //when & then
        assertThatThrownBy(()-> todoService.dragAndDrop(userId,request))
                .isInstanceOf(TodoException.class)
                .hasMessage(TodoExceptionErrorCode.ALREADY_COMPLETED_TODO.getMessage());
    }

    @DisplayName("드래그앤드롭 시 할 일들의 Order를 재정렬한다.")
    @Test
    void 드래그앤드롭_순서_재정렬(){
        //given
        Long userId = 1L;
        DragAndDropRequestDto request = DragAndDropRequestDto.builder()
                .type(Type.TODAY)
                .todoIds(List.of(1L,5L,2L,4L))
                .build();
        //when
        todoService.dragAndDrop(userId,request);
        //then
        Todo TodoId1 = todoRepository.findById(1L).get();
        Todo TodoId5 = todoRepository.findById(5L).get();
        Todo TodoId2 = todoRepository.findById(2L).get();
        Todo TodoId4 = todoRepository.findById(4L).get();

        assertThat(TodoId1.getTodayOrder()).isEqualTo(1L);
        assertThat(TodoId5.getTodayOrder()).isEqualTo(2L);
        assertThat(TodoId2.getTodayOrder()).isEqualTo(3L);
        assertThat(TodoId4.getTodayOrder()).isEqualTo(4L);
    }
}
