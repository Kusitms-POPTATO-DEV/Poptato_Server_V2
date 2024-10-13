package server.poptato.todo.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import server.poptato.global.response.BaseResponse;
import server.poptato.todo.api.response.TodayListResponseDto;
import server.poptato.todo.application.TodoService;

@RestController
@RequiredArgsConstructor
public class TodoController {
    private final TodoService todoService;
    @GetMapping("/todays")
    public BaseResponse<TodayListResponseDto> getTodayList(){
        TodayListResponseDto todayListResponse = todoService.getTodayList();
        return new BaseResponse<>(todayListResponse);
    }
}
