package server.poptato.todo.application.response;

import lombok.Builder;

@Builder
public record YesterdayResponseDto(Long todoId, String content) {
}