package server.poptato.external.kakao.dto.response;

public record KakaoUserResponse(Long id, KakaoUserProperties properties, KakaoAccount kakao_account) {

    public record KakaoUserProperties(String nickname) {
    }

    public record KakaoAccount(String email) {  // 이메일을 담을 필드 추가
    }
}
