package server.poptato.external.kakao.resolver;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import server.poptato.global.exception.BaseException;

import static server.poptato.global.exception.errorcode.BaseExceptionErrorCode.ORIGIN_HEADER_MISSING_EXCEPTION;

@Component
public class OriginResolver implements HandlerMethodArgumentResolver {

    private static final String ORIGIN = "origin";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(OriginHeader.class) && String.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String originHeader = request.getHeader(ORIGIN);
        if (originHeader == null || originHeader.isBlank()) {
            throw new BaseException(ORIGIN_HEADER_MISSING_EXCEPTION);
        }
        return originHeader;
    }
}