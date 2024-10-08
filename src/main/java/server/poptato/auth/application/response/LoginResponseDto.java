package server.poptato.auth.application.dto.response;

public record LoginResponseDto(String accessToken, String refreshToken, boolean isNewUser, Long userId) {
}
