package server.poptato.todo.api;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import server.poptato.global.response.BaseResponse;
import server.poptato.todo.api.request.ContentUpdateRequestDto;
import server.poptato.todo.api.request.DeadlineUpdateRequestDto;
import server.poptato.todo.api.request.DragAndDropRequestDto;
import server.poptato.todo.api.request.SwipeRequestDto;
import server.poptato.todo.application.TodoService;
import server.poptato.todo.application.response.TodoDetailResponseDto;
import server.poptato.user.resolver.UserId;

@RestController
@RequiredArgsConstructor
public class TodoController {
    private final TodoService todoService;

    @DeleteMapping("/todo/{todoId}")
    public BaseResponse deleteTodoById(@UserId Long userId, @PathVariable Long todoId) {
        todoService.deleteTodoById(userId, todoId);
        return new BaseResponse<>();
    }

    @PatchMapping("/swipe")
    public BaseResponse swipe(@UserId Long userId,
                              @Validated @RequestBody SwipeRequestDto swipeRequestDto) {
        todoService.swipe(userId, swipeRequestDto.getTodoId());
        return new BaseResponse<>();
    }

    @PatchMapping("/todo/{todoId}/bookmark")
    public BaseResponse toggleIsBookmark(@UserId Long userId, @PathVariable Long todoId) {
        todoService.toggleIsBookmark(userId, todoId);
        return new BaseResponse<>();
    }

    @PatchMapping("/dragAndDrop")
    public BaseResponse dragAndDrop(@UserId Long userId,
                                    @Validated @RequestBody DragAndDropRequestDto dragAndDropRequestDto) {
        todoService.dragAndDrop(userId, dragAndDropRequestDto);
        return new BaseResponse<>();
    }


    @GetMapping("/todo/{todoId}")
    public BaseResponse<TodoDetailResponseDto> getTodoInfo(@UserId Long userId,
                                                           @PathVariable Long todoId) {
        TodoDetailResponseDto response = todoService.getTodoInfo(userId, todoId);
        return new BaseResponse<>(response);
    }

    @PatchMapping("/todo/{todoId}/deadline")
    public BaseResponse updateDeadline(@UserId Long userId,
                                       @PathVariable Long todoId,
                                       @Validated @RequestBody DeadlineUpdateRequestDto requestDto) {
        todoService.updateDeadline(userId, todoId, requestDto.getDeadline());
        return new BaseResponse<>();
    }

    @PatchMapping("/todo/{todoId}/content")
    public BaseResponse updateContent(@UserId Long userId,
                                      @PathVariable Long todoId,
                                      @Validated @RequestBody ContentUpdateRequestDto requestDto) {
        todoService.updateContent(userId, todoId, requestDto.getContent());
        return new BaseResponse<>();
    }

    @PatchMapping("/todo/{todoId}/achieve")
    public BaseResponse updateIsCompleted(@UserId Long userId,
                                          @PathVariable Long todoId) {
        todoService.updateIsCompleted(userId, todoId);
        return new BaseResponse<>();
    }
}
