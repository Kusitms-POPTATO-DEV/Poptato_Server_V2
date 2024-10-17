package server.poptato.todo.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ContentUpdateRequestDto {
    @NotBlank(message = "할 일 수정 시 내용은 필수입니다.")
    String content;
}
