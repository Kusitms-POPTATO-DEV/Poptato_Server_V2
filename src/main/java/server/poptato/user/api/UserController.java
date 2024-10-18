package server.poptato.user.api;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import server.poptato.global.response.BaseResponse;
import server.poptato.user.api.request.UserChangeNameRequestDto;
import server.poptato.user.application.response.UserResponseDto;
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
        return new BaseResponse();
    }
    @PatchMapping("/mypage")
    public BaseResponse updateUserName(@UserId Long userId, @Validated @RequestBody UserChangeNameRequestDto request) {
        userService.updateUserName(userId, request.getNewName());
        return new BaseResponse();
    }
    @GetMapping("/mypage")
    public BaseResponse getUserNameAndEmail(@UserId Long userId) {
        UserResponseDto response = userService.getUserNameAndEmailById(userId);
        return new BaseResponse(response);
    }
}