package server.poptato.todo.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import server.poptato.global.response.BaseResponse;
import server.poptato.todo.api.request.SwipeRequestDto;
import server.poptato.todo.application.response.BacklogListResponseDto;
import server.poptato.todo.application.response.TodayListResponseDto;
import server.poptato.todo.application.TodoService;
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

    @GetMapping("/backlogs")
    public BaseResponse<BacklogListResponseDto> getBacklogList(
            @UserId Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "8") int size){
        BacklogListResponseDto backlogListResponse = todoService.getBacklogList(userId, page, size);
        return new BaseResponse<>(backlogListResponse);
    }

    @PatchMapping("/swipe")
    public BaseResponse swipe(@UserId Long userId, @RequestBody SwipeRequestDto swipeRequestDto){
        todoService.swipe(userId, swipeRequestDto.getTodoId());
        return new BaseResponse<>();
    }
}
