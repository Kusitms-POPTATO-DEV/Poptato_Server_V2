package server.poptato.auth.application.dto.request;

public record TokenRequestDto(String accessToken, String refreshToken) {
}
