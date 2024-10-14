package server.poptato.todo.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import server.poptato.global.response.BaseResponse;
import server.poptato.todo.api.response.TodayListResponseDto;
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
}
