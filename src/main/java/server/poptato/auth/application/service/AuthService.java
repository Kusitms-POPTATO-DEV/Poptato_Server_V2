package server.poptato.auth.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import server.poptato.auth.api.request.TokenRequestDto;
import server.poptato.auth.application.response.LoginResponseDto;
import server.poptato.auth.exception.AuthException;
import server.poptato.external.kakao.dto.response.KakaoUserInfo;
import server.poptato.external.kakao.service.KakaoSocialService;
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
    private final KakaoSocialService kakaoSocialService;
    private final UserRepository userRepository;

    public LoginResponseDto login(final String accessToken) {
        KakaoUserInfo info  = kakaoSocialService.getIdAndNickNameAndEmailFromKakao(accessToken);
        String kakaoId = info.kakaoId();
        String name = info.nickname();
        String email = info.email();
        Optional<User> user = userRepository.findByKakaoId(kakaoId);
        if (user.isEmpty()) {
            return createNewUserResponse(kakaoId, name, email);
        }
        TokenPair tokenPair = jwtService.generateTokenPair(String.valueOf(user.get().getId()));
        return new LoginResponseDto(tokenPair.accessToken(), tokenPair.refreshToken(), false, user.get().getId());
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

    private LoginResponseDto createNewUserResponse(String kakaoId, String name, String email) {
        User newUser = User.builder()
                .kakaoId(kakaoId)
                .name(name)
                .email(email)
                .build();
        userRepository.save(newUser);

        // 토큰 발급 및 응답 객체 생성
        TokenPair tokenPair = jwtService.generateTokenPair(String.valueOf(newUser.getId()));
        return new LoginResponseDto(tokenPair.accessToken(), tokenPair.refreshToken(), true, newUser.getId());
    }
}
