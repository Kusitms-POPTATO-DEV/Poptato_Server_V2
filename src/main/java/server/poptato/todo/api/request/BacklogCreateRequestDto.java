package server.poptato.todo.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class BacklogCreateRequestDto {
    @NotBlank(message = "백로그 생성 시 내용은 필수입니다.")
    String content;
}
