package server.poptato.todo.application.response;

import lombok.Builder;

import java.util.List;

@Builder
public record PaginatedHistoryResponseDto(List<HistoryResponseDto> histories, int totalPageCount) {
}