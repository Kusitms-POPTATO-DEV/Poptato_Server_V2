package server.poptato.todo.application;

import org.springframework.stereotype.Service;
import server.poptato.todo.api.response.TodayListResponseDto;

@Service
public class TodoService {
    public TodayListResponseDto getTodayList(long userId, int page, int size) {
        return null;
    }
}
