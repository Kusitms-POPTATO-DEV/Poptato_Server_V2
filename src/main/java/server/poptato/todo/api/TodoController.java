package server.poptato.todo.api;

import com.sun.net.httpserver.Authenticator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.poptato.global.response.BaseResponse;
import server.poptato.todo.application.service.TodoService;

import static server.poptato.global.exception.errorcode.BaseExceptionErrorCode.SUCCESS;

@RestController
@RequestMapping("/todo")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @DeleteMapping("/{todoId}")
    public BaseResponse deleteTodoById(@PathVariable Long todoId) {
        todoService.deleteTodoById(todoId);
        return new BaseResponse<>();
    }
}