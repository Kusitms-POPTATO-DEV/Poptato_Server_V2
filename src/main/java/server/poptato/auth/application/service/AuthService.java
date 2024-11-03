package server.poptato.auth.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import server.poptato.auth.api.request.KakaoLoginRequestDto;
import server.poptato.auth.api.request.ReissueTokenRequestDto;
import server.poptato.auth.application.response.LoginResponseDto;
import server.poptato.auth.converter.AuthDtoConverter;
import server.poptato.external.oauth.SocialPlatform;
import server.poptato.external.oauth.SocialService;
import server.poptato.external.oauth.SocialServiceProvider;
import server.poptato.external.oauth.SocialUserInfo;
import server.poptato.global.dto.TokenPair;
import server.poptato.user.domain.entity.User;
import server.poptato.user.domain.repository.UserRepository;
import server.poptato.user.validator.UserValidator;

import java.util.Optional;

import static server.poptato.external.oauth.SocialPlatform.KAKAO;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtService jwtService;
    private final SocialServiceProvider socialServiceProvider;
    private final UserRepository userRepository;
    private final UserValidator userValidator;

    public LoginResponseDto login(final KakaoLoginRequestDto loginRequestDto) {
        //TODO: 나중에 없앨 코드
        String accessToken = loginRequestDto.getKakaoCode();
        SocialPlatform socialPlatform = KAKAO;

        SocialService socialService = socialServiceProvider.getSocialService(socialPlatform);
        SocialUserInfo userInfo = socialService.getUserData(accessToken);
        Optional<User> user = userRepository.findByKakaoId(userInfo.socialId());
        if (user.isEmpty()) {
            return createNewUserResponse(userInfo);
        }
        return createOldUserResponse(user.get());
    }

    public void logout(final Long userId) {
        userValidator.checkIsExistUser(userId);
        jwtService.deleteRefreshToken(String.valueOf(userId));
    }

    public TokenPair refresh(final ReissueTokenRequestDto reissueTokenRequestDto) {
        checkIsValidToken(reissueTokenRequestDto.getRefreshToken());

        final String userId = jwtService.getUserIdInToken(reissueTokenRequestDto.getRefreshToken());
        userValidator.checkIsExistUser(Long.parseLong(userId));

        final TokenPair tokenPair = jwtService.generateTokenPair(userId);
        jwtService.saveRefreshToken(userId, tokenPair.refreshToken());

        return tokenPair;
    }

    private void checkIsValidToken(String refreshToken) {
        try{
            jwtService.verifyToken(refreshToken);
            jwtService.compareRefreshToken(jwtService.getUserIdInToken(refreshToken), refreshToken);
        }catch(Exception e){
            throw e;
        }
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
        return AuthDtoConverter.toLoginDto(tokenPair, user, isNewUser);
    }
}
