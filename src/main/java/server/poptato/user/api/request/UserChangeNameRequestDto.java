package server.poptato.user.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserChangeNameRequestDto {
    @NotBlank(message = "이름은 빈 값일 수 없습니다.")
    String newName;
}
