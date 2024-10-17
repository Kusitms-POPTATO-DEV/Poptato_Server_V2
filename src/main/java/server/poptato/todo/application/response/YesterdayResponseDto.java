package server.poptato.todo.application.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class YesterdayResponseDto {
    private Long todoId;
    private String content;
}