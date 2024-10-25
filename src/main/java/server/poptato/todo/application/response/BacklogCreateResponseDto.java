package server.poptato.todo.application.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class BacklogCreateResponseDto {
    Long todoId;
}
