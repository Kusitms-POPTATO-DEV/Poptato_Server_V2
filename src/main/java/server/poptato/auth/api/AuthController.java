package server.poptato.auth.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.poptato.auth.api.request.KakaoLoginRequestDto;
import server.poptato.auth.api.request.TokenRequestDto;
import server.poptato.auth.application.response.LoginResponseDto;
import server.poptato.auth.application.service.AuthService;
import server.poptato.external.oauth.SocialPlatform;
import server.poptato.global.dto.TokenPair;
import server.poptato.global.response.BaseResponse;
import server.poptato.user.resolver.UserId;

import static server.poptato.external.oauth.SocialPlatform.KAKAO;
import static server.poptato.global.exception.errorcode.BaseExceptionErrorCode.SUCCESS;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public BaseResponse<LoginResponseDto> login(@RequestBody KakaoLoginRequestDto kakaoLoginRequestDto) {
        String accessToken = kakaoLoginRequestDto.kakaoCode();
        SocialPlatform socialPlatform = KAKAO;
        LoginResponseDto response = authService.login(accessToken, socialPlatform);
        return new BaseResponse<>(response);
    }

    @PostMapping("/logout")
    public BaseResponse logout(@UserId Long userId) {
        authService.logout(userId);
        return new BaseResponse(SUCCESS);
    }
    @PostMapping("/refresh")
    public BaseResponse<TokenPair> refresh(@RequestBody final TokenRequestDto tokenRequestDto) {
        TokenPair response = authService.refresh(tokenRequestDto);
        return new BaseResponse<>(response);
    }
}
