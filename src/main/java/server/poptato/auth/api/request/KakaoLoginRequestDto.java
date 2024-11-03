package server.poptato.auth.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoLoginRequestDto {
    @NotBlank(message = "kakaoCode는 필수입니다.")
    String kakaoCode;
}
