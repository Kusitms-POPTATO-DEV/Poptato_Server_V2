package server.poptato.global.dto;

public record UserCreateResponse(
        String accessToken,
        String refreshToken
) {
}
