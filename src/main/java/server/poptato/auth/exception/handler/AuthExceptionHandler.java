package server.poptato.auth.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import server.poptato.auth.exception.AuthException;
import server.poptato.global.response.BaseErrorResponse;
import server.poptato.user.exception.UserException;

@Slf4j
@Order(0)
@RestControllerAdvice
public class AuthExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(AuthException.class)
    public BaseErrorResponse handleUserException(AuthException e) {
        log.error("[UserException: handle_UserException 호출]", e);
        return new BaseErrorResponse(e.getExceptionStatus(), e.getMessage());
    }
}
