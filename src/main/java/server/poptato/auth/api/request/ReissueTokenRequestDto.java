package server.poptato.auth.api.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReissueTokenRequestDto {
    @NotEmpty(message = "토큰 재발급 시 accessToken은 필수입니다.")
    String accessToken;
    @NotEmpty(message = "토큰 재발급 시 refreshToken은 필수입니다.")
    String refreshToken;
}
