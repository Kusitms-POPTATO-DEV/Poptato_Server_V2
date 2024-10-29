package server.poptato.todo.converter;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import server.poptato.todo.application.response.BacklogListResponseDto;
import server.poptato.todo.application.response.TodayListResponseDto;
import server.poptato.todo.domain.entity.Todo;

import java.time.LocalDate;
import java.util.List;

@Component
public class TodoDtoConverter {
    public static TodayListResponseDto toTodayListDto(LocalDate todayDate, List<Todo> todaySubList, int totalPageCount) {
        return TodayListResponseDto.builder()
                .date(todayDate)
                .todays(todaySubList)
                .totalPageCount(totalPageCount)
                .build();
    }

    public static BacklogListResponseDto toBacklogListDto(Page<Todo> backlogs) {
        return BacklogListResponseDto.builder()
                .totalCount(backlogs.getTotalElements())
                .backlogs(backlogs.getContent())
                .totalPageCount(backlogs.getTotalPages())
                .build();
    }

    private TodoDtoConverter() {
    }
}
