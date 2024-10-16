package server.poptato.todo.exception.errorcode;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import server.poptato.global.response.status.ResponseStatus;

@RequiredArgsConstructor
public enum TodoExceptionErrorCode implements ResponseStatus {

    /**
     * 5000: Todo 도메인 오류
     */

    INVALID_PAGE(5000, HttpStatus.BAD_REQUEST.value(), "유효햐지 않은 페이지 수입니다.");

    private final int code;
    private final int status;
    private final String message;


    @Override
    public int getCode() {
        return code;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

