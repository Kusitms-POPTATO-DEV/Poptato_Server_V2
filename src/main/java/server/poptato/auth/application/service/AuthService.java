package server.poptato.auth.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import server.poptato.auth.application.dto.request.TokenRequestDto;
import server.poptato.auth.application.dto.response.LoginResponseDto;
import server.poptato.config.jwt.JwtService;
import server.poptato.external.kakao.service.KakaoSocialService;
import server.poptato.global.dto.TokenPair;
import server.poptato.global.dto.UserCreateResponse;
import server.poptato.global.exception.BaseException;
import server.poptato.user.domain.entity.User;
import server.poptato.user.domain.repository.UserRepository;

import java.util.Optional;

import static server.poptato.global.exception.errorcode.BaseExceptionErrorCode.TOKEN_TIME_EXPIRED_EXCEPTION;
import static server.poptato.global.exception.errorcode.BaseExceptionErrorCode.USER_NOT_FOUND_EXCEPTION;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtService jwtService;
    private final KakaoSocialService kakaoSocialService;
    private final UserRepository userRepository;

    public LoginResponseDto login(final String baseUrl, final String kakaoCode) {
        String kakaoId = kakaoSocialService.getIdFromKakao(baseUrl, kakaoCode);
        Optional<User> user = userRepository.findByKakaoId(kakaoId);
        if (user.isEmpty()) {
            User newUser = User.builder()
                    .kakaoId(kakaoId)
                    .build();
            userRepository.save(newUser);

            TokenPair tokenPair = jwtService.generateTokenPair(String.valueOf(newUser.getId()));
            return new LoginResponseDto(tokenPair.accessToken(), tokenPair.refreshToken(), true);
        }
        TokenPair tokenPair = jwtService.generateTokenPair(String.valueOf(user.get().getId()));
        return new LoginResponseDto(tokenPair.accessToken(), tokenPair.refreshToken(), false);
    }

    public UserCreateResponse createUserToken(String useId) {

        TokenPair tokenPair = jwtService.generateTokenPair(useId);
        UserCreateResponse userCreateResponse = new UserCreateResponse(tokenPair.accessToken(), tokenPair.refreshToken());

        return userCreateResponse;
    }



    public void logout(final Long userId) {
        final User user = userRepository.findById(userId).orElseThrow(() -> new BaseException(USER_NOT_FOUND_EXCEPTION));
        jwtService.deleteRefreshToken(String.valueOf(userId));
    }

    public TokenPair refresh(final TokenRequestDto tokenRequestDto) {
        if (!jwtService.verifyToken(tokenRequestDto.refreshToken()))
            throw new BaseException(TOKEN_TIME_EXPIRED_EXCEPTION);

        final String userId = jwtService.getUserIdInToken(tokenRequestDto.refreshToken());
        final User user = userRepository.findById(Long.parseLong(userId)).orElseThrow(() -> new BaseException(USER_NOT_FOUND_EXCEPTION));

        if (!jwtService.compareRefreshToken(userId, tokenRequestDto.refreshToken()))
            throw new BaseException(TOKEN_TIME_EXPIRED_EXCEPTION);

        final TokenPair tokenPair = jwtService.generateTokenPair(userId);
        jwtService.saveRefreshToken(userId, tokenPair.refreshToken());
        return tokenPair;
    }
}
