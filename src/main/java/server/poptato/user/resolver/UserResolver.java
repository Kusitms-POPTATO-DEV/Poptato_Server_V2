package server.poptato.user.resolver;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import server.poptato.auth.application.service.JwtService;
import server.poptato.global.exception.BaseException;
import server.poptato.global.response.BaseErrorResponse;
import server.poptato.user.resolver.UserId;

import static server.poptato.global.exception.errorcode.BaseExceptionErrorCode.*;


@Component
@RequiredArgsConstructor
public class UserResolver implements HandlerMethodArgumentResolver {
    private final JwtService jwtService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(UserId.class) && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        final HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        final String token = request.getHeader("Authorization");
        if (token == null || token.isBlank() || !token.startsWith("Bearer ")) {
            throw new BaseException(TOKEN_NOT_CONTAINED_EXCEPTION);
        }
        final String encodedUserId = token.substring("Bearer ".length());
        if (!jwtService.verifyToken(encodedUserId)) {
            throw new BaseException(TOKEN_TIME_EXPIRED_EXCEPTION);
        }
        final String decodedUserId = jwtService.getUserIdInToken(encodedUserId);
        try {
            return Long.parseLong(decodedUserId);
        } catch (NumberFormatException e) {
            return new BaseErrorResponse(INVALID_TOKEN_EXCEPTION);
        }
    }
}
