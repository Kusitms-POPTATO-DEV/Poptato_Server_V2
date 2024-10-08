package server.poptato.external.kakao.resolver;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import server.poptato.global.exception.BaseException;

import static server.poptato.global.exception.errorcode.BaseExceptionErrorCode.EMPTY_KAKAO_CODE_EXCEPTION;


@Component
@RequiredArgsConstructor
public class KakaoCodeResolver implements HandlerMethodArgumentResolver {
    private static final String AUTHORIZATION = "authorization";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(KakaoCode.class) && String.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        final HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        final String token = request.getHeader(AUTHORIZATION);
        if (token == null || token.isBlank() || !token.startsWith("Bearer ")) {
            throw new BaseException(EMPTY_KAKAO_CODE_EXCEPTION);
        }
        return token.substring("Bearer ".length());
    }
}
