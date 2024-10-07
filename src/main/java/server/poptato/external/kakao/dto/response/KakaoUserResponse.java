package server.poptato.external.kakao.dto.response;

public record KakaoUserResponse(Long id, KakaoUserProperties properties) {

    public record KakaoUserProperties(String nickname) {
    }
}