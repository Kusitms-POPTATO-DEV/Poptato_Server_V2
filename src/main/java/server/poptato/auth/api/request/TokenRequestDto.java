package server.poptato.auth.api.request;

public record TokenRequestDto(String accessToken, String refreshToken) {
}
