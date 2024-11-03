package server.poptato.auth.converter;

import org.springframework.stereotype.Component;
import server.poptato.auth.application.response.LoginResponseDto;
import server.poptato.global.dto.TokenPair;
import server.poptato.user.domain.entity.User;

@Component
public class AuthDtoConverter {
    public static LoginResponseDto toLoginDto(TokenPair tokenPair, User user, boolean isNewUser ){
        return LoginResponseDto.builder()
                .accessToken(tokenPair.accessToken())
                .refreshToken(tokenPair.refreshToken())
                .isNewUser(isNewUser)
                .userId(user.getId())
                .build();
    }

    private AuthDtoConverter() {
    }
}
