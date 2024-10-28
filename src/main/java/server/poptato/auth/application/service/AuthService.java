package server.poptato.auth.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import server.poptato.auth.api.request.TokenRequestDto;
import server.poptato.auth.application.response.LoginResponseDto;
import server.poptato.auth.exception.AuthException;
import server.poptato.external.oauth.SocialPlatform;
import server.poptato.external.oauth.SocialService;
import server.poptato.external.oauth.SocialServiceProvider;
import server.poptato.external.oauth.SocialUserInfo;
import server.poptato.global.dto.TokenPair;
import server.poptato.user.domain.entity.User;
import server.poptato.user.domain.repository.UserRepository;
import server.poptato.user.exception.UserException;

import java.util.Optional;

import static server.poptato.auth.exception.errorcode.AuthExceptionErrorCode.TOKEN_TIME_EXPIRED;
import static server.poptato.user.exception.errorcode.UserExceptionErrorCode.USER_NOT_EXIST;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtService jwtService;
    private final SocialServiceProvider socialServiceProvider;
    private final UserRepository userRepository;

    public LoginResponseDto login(final String accessToken, final SocialPlatform socialPlatform) {
        SocialService socialService = socialServiceProvider.getSocialService(socialPlatform);
        SocialUserInfo userInfo = socialService.getUserData(accessToken);
        Optional<User> user = userRepository.findByKakaoId(userInfo.socialId());
        if (user.isEmpty()) {
            return createNewUserResponse(userInfo);
        }
        return createOldUserResponse(user.get());
    }

    public void logout(final Long userId) {
        final User user = userRepository.findById(userId).orElseThrow(() -> new UserException(USER_NOT_EXIST));
        jwtService.deleteRefreshToken(String.valueOf(userId));
    }

    public TokenPair refresh(final TokenRequestDto tokenRequestDto) {
        if (!jwtService.verifyToken(tokenRequestDto.refreshToken()))
            throw new AuthException(TOKEN_TIME_EXPIRED);

        final String userId = jwtService.getUserIdInToken(tokenRequestDto.refreshToken());
        final User user = userRepository.findById(Long.parseLong(userId)).orElseThrow(() -> new UserException(USER_NOT_EXIST));

        if (!jwtService.compareRefreshToken(userId, tokenRequestDto.refreshToken()))
            throw new AuthException(TOKEN_TIME_EXPIRED);

        final TokenPair tokenPair = jwtService.generateTokenPair(userId);
        jwtService.saveRefreshToken(userId, tokenPair.refreshToken());
        return tokenPair;
    }

    private LoginResponseDto createNewUserResponse(SocialUserInfo userInfo) {
        User newUser = User.builder()
                .kakaoId(userInfo.socialId())
                .name(userInfo.nickname())
                .email(userInfo.email())
                .build();
        userRepository.save(newUser);

        return createLoginResponse(newUser, true);
    }

    private LoginResponseDto createOldUserResponse(User user) {
        return createLoginResponse(user, false);
    }

    private LoginResponseDto createLoginResponse(User user, boolean isNewUser) {
        TokenPair tokenPair = jwtService.generateTokenPair(String.valueOf(user.getId()));
        return new LoginResponseDto(tokenPair.accessToken(), tokenPair.refreshToken(), isNewUser, user.getId());
    }
}
