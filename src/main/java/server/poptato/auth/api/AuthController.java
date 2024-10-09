package server.poptato.auth.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.poptato.auth.api.request.TokenRequestDto;
import server.poptato.auth.application.response.LoginResponseDto;
import server.poptato.auth.application.service.AuthService;
import server.poptato.external.kakao.resolver.KakaoCode;
import server.poptato.external.kakao.resolver.OriginHeader;
import server.poptato.global.dto.TokenPair;
import server.poptato.global.response.BaseResponse;
import server.poptato.user.resolver.UserId;

import static server.poptato.global.exception.errorcode.BaseExceptionErrorCode.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public BaseResponse<LoginResponseDto> login(
            @KakaoCode String kakaoCode,
            @OriginHeader String originHeader) {
        LoginResponseDto response = authService.login(originHeader, kakaoCode);
        return new BaseResponse<>(SUCCESS, response);
    }

    @PostMapping("/logout")
    public BaseResponse logout(@UserId Long userId) {
        authService.logout(userId);
        return new BaseResponse(SUCCESS);
    }
    @PostMapping("/refresh")
    public BaseResponse<TokenPair> refresh(@RequestBody final TokenRequestDto tokenRequestDto) {
        TokenPair response = authService.refresh(tokenRequestDto);
        return new BaseResponse<>(SUCCESS, response);
    }
}
