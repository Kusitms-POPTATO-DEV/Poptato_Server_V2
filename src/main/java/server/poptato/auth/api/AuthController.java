package server.poptato.auth.api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.poptato.auth.application.dto.response.LoginResponseDto;
import server.poptato.config.resolver.kakao.KakaoCode;
import server.poptato.global.response.BaseResponse;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String ORIGIN = "origin";
    private final AuthService authService;

    @PostMapping("/login")
    public BaseResponse<LoginResponseDto> login(
            @KakaoCode String kakaoCode,
            HttpServletRequest request) {
        String originHeader = request.getHeader(ORIGIN);
        authService.login(originHeader, kakaoCode)
        return BaseResponse.success(SOCIAL_LOGIN_SUCCESS, );
    }

    @PostMapping("/logout")
    public SuccessNonDataResponse logout(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");  // `userId`를 헤더에서 가져온다고 가정
        authService.logout(userId);
        return SuccessNonDataResponse.success(LOGOUT_SUCCESS);
    }

    @PostMapping("/refresh")
    public SuccessResponse<TokenPair> refresh(@RequestBody final TokenRequestDto tokenRequestDto) {
        return SuccessResponse.success(REFRESH_SUCCESS, authService.refresh(tokenRequestDto));
    }
}
