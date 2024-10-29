package server.poptato.auth.api.request;

public record ReissueTokenRequestDto(String accessToken, String refreshToken) {
}
