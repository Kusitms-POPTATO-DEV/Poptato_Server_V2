package server.poptato.auth.api.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoLoginRequestDto {
    @NotEmpty(message = "kakaoCode는 필수입니다.")
    String kakaoCode;
}
