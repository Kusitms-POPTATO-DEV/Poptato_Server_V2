package server.poptato.user.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.poptato.global.response.BaseResponse;
import server.poptato.user.application.service.UserService;
import server.poptato.user.resolver.UserId;

import static server.poptato.global.exception.errorcode.BaseExceptionErrorCode.SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @DeleteMapping
    public BaseResponse deleteUser(@UserId Long userId) {
        userService.deleteUser(userId);
        return new BaseResponse(SUCCESS);
    }
}