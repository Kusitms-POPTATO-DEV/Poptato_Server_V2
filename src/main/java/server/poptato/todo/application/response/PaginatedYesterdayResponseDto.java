package server.poptato.todo.application.response;

import lombok.Builder;

import java.util.List;

@Builder
public record PaginatedYesterdayResponseDto(List<YesterdayResponseDto> yesterdays,int totalPageCount) {
}
