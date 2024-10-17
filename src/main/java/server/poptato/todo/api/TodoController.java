package server.poptato.todo.api;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import server.poptato.global.response.BaseResponse;
import server.poptato.todo.api.request.BacklogCreateRequestDto;
import server.poptato.todo.api.request.DragAndDropRequestDto;
import server.poptato.todo.api.request.SwipeRequestDto;
import server.poptato.todo.application.TodoService;
import server.poptato.todo.application.response.BacklogListResponseDto;
import server.poptato.todo.application.response.TodayListResponseDto;
import server.poptato.user.resolver.UserId;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class TodoController {
    private final TodoService todoService;
    @GetMapping("/todays")
    public BaseResponse<TodayListResponseDto> getTodayList(
            @UserId Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "8") int size){
        LocalDate todayDate = LocalDate.now();
        TodayListResponseDto todayListResponse = todoService.getTodayList(userId, page, size, todayDate);
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

    @PatchMapping("/swipe")
    public BaseResponse swipe(@UserId Long userId,
                              @Validated @RequestBody SwipeRequestDto swipeRequestDto){
        todoService.swipe(userId, swipeRequestDto.getTodoId());
        return new BaseResponse<>();
    }
    @PatchMapping("/todo/{todoId}/bookmark")
    public BaseResponse toggleIsBookmark(@PathVariable Long todoId) {
        todoService.toggleIsBookmark(todoId);
        return new BaseResponse<>();
    }

    @PatchMapping("/dragAndDrop")
    public BaseResponse dragAndDrop(@UserId Long userId,
                                    @Validated @RequestBody DragAndDropRequestDto dragAndDropRequestDto){
        todoService.dragAndDrop(userId, dragAndDropRequestDto);
        return new BaseResponse<>();
    }

    @PostMapping("/backlog")
    public BaseResponse generateBacklog(//@UserId Long userId,
                                        @Validated @RequestBody BacklogCreateRequestDto backlogCreateRequestDto){
        todoService.generateBacklog(1L, backlogCreateRequestDto.getContent());
        return new BaseResponse<>();
    }
}
