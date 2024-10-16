package server.poptato.todo.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import server.poptato.global.response.BaseResponse;
import server.poptato.todo.application.TodoService;
import server.poptato.todo.application.response.BacklogListResponseDto;
import server.poptato.todo.application.response.TodayListResponseDto;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.user.resolver.UserId;

@RestController
@RequiredArgsConstructor
public class TodoController {
    private final TodoService todoService;
    @GetMapping("/todays")
    public BaseResponse<TodayListResponseDto> getTodayList(
            @UserId Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "8") int size){
        TodayListResponseDto todayListResponse = todoService.getTodayList(userId, page, size);
        return new BaseResponse<>(todayListResponse);
    }

    @DeleteMapping("/todo/{todoId}")
    public BaseResponse deleteTodoById(@PathVariable Long todoId) {
        todoService.deleteTodoById(todoId);
        return new BaseResponse<>();
    }

    @GetMapping("/backlogs")
    public BaseResponse<BacklogListResponseDto> getBacklogList(
            @UserId Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "8") int size){
        BacklogListResponseDto backlogListResponse = todoService.getBacklogList(userId, page, size);
        return new BaseResponse<>(backlogListResponse);
    }
    @PatchMapping("/todo/{todoId}/bookmark")
    public BaseResponse toggleIsBookmark(@PathVariable Long todoId) {
        todoService.toggleIsBookmark(todoId);
        return new BaseResponse<>();
    }
}
