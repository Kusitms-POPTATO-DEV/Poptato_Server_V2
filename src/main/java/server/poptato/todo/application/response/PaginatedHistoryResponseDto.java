package server.poptato.todo.application.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PaginatedHistoryResponseDto {
    private List<HistoryResponseDto> histories;
    private int totalPageCount;
}