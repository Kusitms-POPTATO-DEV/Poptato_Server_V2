package server.poptato.auth.api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.poptato.auth.application.dto.request.TokenRequestDto;
import server.poptato.auth.application.dto.response.LoginResponseDto;
import server.poptato.auth.application.service.AuthService;
import server.poptato.config.resolver.kakao.KakaoCode;
import server.poptato.global.dto.TokenPair;
import server.poptato.global.response.BaseResponse;
import server.poptato.global.response.status.ResponseStatus;

import static server.poptato.global.exception.errorcode.BaseExceptionErrorCode.*;

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
        LoginResponseDto response = authService.login(originHeader, kakaoCode);
        if (response.isNewUser()) {
            return new BaseResponse<>(SUCCESS_REGISTER, response);  // 회원가입 성공 응답
        } else {
            return new BaseResponse<>(SUCCESS_LOGIN, response);  // 로그인 성공 응답
        }
    }

    @PostMapping("/logout")
    public BaseResponse logout(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");  // `userId`를 헤더에서 가져온다고 가정
        authService.logout(userId);
        return new BaseResponse(SUCCESS);
    }

    @PostMapping("/refresh")
    public BaseResponse<TokenPair> refresh(@RequestBody final TokenRequestDto tokenRequestDto) {
        TokenPair response = authService.refresh(tokenRequestDto);
        return new BaseResponse<>(response);
    }
}
