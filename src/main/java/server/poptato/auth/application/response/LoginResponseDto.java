package server.poptato.auth.application.response;

public record LoginResponseDto(String accessToken, String refreshToken, boolean isNewUser, Long userId) {
}
