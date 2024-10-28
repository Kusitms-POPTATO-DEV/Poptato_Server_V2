package server.poptato.external.oauth.kakao.dto.response;

public record KakaoUserResponse(Long id, KakaoUserProperties properties, KakaoAccount kakao_account) {

    public record KakaoUserProperties(String nickname) {
    }

    public record KakaoAccount(String email) {
    }
}
