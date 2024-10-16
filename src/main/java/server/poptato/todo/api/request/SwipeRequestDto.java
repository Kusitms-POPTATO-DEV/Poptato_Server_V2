package server.poptato.todo.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SwipeRequestDto {
    @NotBlank(message = "할일 ID는 필수입니다.")
    Long todoId;
}
