package server.poptato.todo.application.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PaginatedYesterdayResponseDto {
    private List<YesterdayResponseDto> yesterdays;
    private int totalPageCount;
}
