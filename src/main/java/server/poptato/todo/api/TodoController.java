package server.poptato.todo.api;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import server.poptato.global.response.BaseResponse;
import server.poptato.todo.api.request.*;
import server.poptato.todo.application.TodoService;
import server.poptato.todo.application.response.*;
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
    public BaseResponse deleteTodoById(@UserId Long userId, @PathVariable Long todoId) {
        todoService.deleteTodoById(userId, todoId);
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
    public BaseResponse toggleIsBookmark(@UserId Long userId, @PathVariable Long todoId) {
        todoService.toggleIsBookmark(userId, todoId);
        return new BaseResponse<>();
    }
    @GetMapping("/histories")
    public BaseResponse<PaginatedHistoryResponseDto> getHistories(
            @UserId Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size) {
        PaginatedHistoryResponseDto response = todoService.getHistories(userId, page, size);

        return new BaseResponse<>(response);
    }
    @PatchMapping("/dragAndDrop")
    public BaseResponse dragAndDrop(@UserId Long userId,
                                    @Validated @RequestBody DragAndDropRequestDto dragAndDropRequestDto){
        todoService.dragAndDrop(userId, dragAndDropRequestDto);
        return new BaseResponse<>();
    }
    @GetMapping("/yesterdays")
    public BaseResponse<PaginatedYesterdayResponseDto> getYesterdays(
            @UserId Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size) {

        PaginatedYesterdayResponseDto response = todoService.getYesterdays(userId, page, size);
        return new BaseResponse<>(response);
    }

    @PostMapping("/backlog")
    public BaseResponse<BacklogCreateResponseDto> generateBacklog(@UserId Long userId,
                                        @Validated @RequestBody BacklogCreateRequestDto backlogCreateRequestDto){
        BacklogCreateResponseDto response = todoService.generateBacklog(userId, backlogCreateRequestDto.getContent());
        return new BaseResponse<>(response);
    }

    @GetMapping("/todo/{todoId}")
    public BaseResponse<TodoDetailResponseDto> getTodoInfo(@UserId Long userId,
                                                           @PathVariable Long todoId){
        TodoDetailResponseDto response = todoService.getTodoInfo(userId, todoId);
        return new BaseResponse<>(response);
    }

    @PatchMapping("/todo/{todoId}/deadline")
    public BaseResponse updateDeadline(@UserId Long userId,
                                       @PathVariable Long todoId,
                                       @Validated @RequestBody DeadlineUpdateRequestDto requestDto){
        todoService.updateDeadline(userId, todoId, requestDto.getDeadline());
        return new BaseResponse<>();
    }

    @PatchMapping("/todo/{todoId}/content")
    public BaseResponse updateContent(@UserId Long userId,
                                       @PathVariable Long todoId,
                                       @Validated @RequestBody ContentUpdateRequestDto requestDto){
        todoService.updateContent(userId, todoId, requestDto.getContent());
        return new BaseResponse<>();
    }

    @PatchMapping("/todo/{todoId}/achieve")
    public BaseResponse updateIsCompleted(@UserId Long userId,
                                          @PathVariable Long todoId){
        todoService.updateIsCompleted(userId, todoId);
        return new BaseResponse<>();
    }
}
