package server.poptato.auth.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import server.poptato.auth.api.request.KakaoLoginRequestDto;
import server.poptato.auth.api.request.ReissueTokenRequestDto;
import server.poptato.auth.application.response.LoginResponseDto;
import server.poptato.auth.converter.AuthDtoConverter;
import server.poptato.external.oauth.SocialPlatform;
import server.poptato.external.oauth.SocialService;
import server.poptato.external.oauth.SocialServiceProvider;
import server.poptato.external.oauth.SocialUserInfo;
import server.poptato.global.dto.TokenPair;
import server.poptato.todo.constant.TutorialMessage;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.repository.TodoRepository;
import server.poptato.todo.domain.value.TodayStatus;
import server.poptato.todo.domain.value.Type;
import server.poptato.user.domain.entity.User;
import server.poptato.user.domain.repository.UserRepository;
import server.poptato.user.validator.UserValidator;

import java.time.LocalDate;
import java.util.Optional;

import static server.poptato.external.oauth.SocialPlatform.KAKAO;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtService jwtService;
    private final SocialServiceProvider socialServiceProvider;
    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final TodoRepository todoRepository;

    public LoginResponseDto login(final KakaoLoginRequestDto loginRequestDto) {
        String accessToken = loginRequestDto.getKakaoCode();
        SocialPlatform socialPlatform = KAKAO;

        SocialService socialService = socialServiceProvider.getSocialService(socialPlatform);
        SocialUserInfo userInfo = socialService.getUserData(accessToken);
        Optional<User> user = userRepository.findByKakaoId(userInfo.socialId());
        if (user.isEmpty()) {
            LoginResponseDto response = createNewUserResponse(userInfo);
            createTutorialData(response.userId());
            return response;
        }
        return createOldUserResponse(user.get(), userInfo);
    }

    private LoginResponseDto createNewUserResponse(SocialUserInfo userInfo) {
        User newUser = User.builder()
                .kakaoId(userInfo.socialId())
                .name(userInfo.nickname())
                .email(userInfo.email())
                .imageUrl(userInfo.imageUrl())
                .build();
        userRepository.save(newUser);

        return createLoginResponse(newUser, true);
    }

    public void createTutorialData(Long userId) {
        createAndSaveTodo(Type.TODAY, userId, 1, TutorialMessage.TODAY_COMPLETE);
        for (int i = 0; i < 4; i++) {
            createAndSaveTodo(Type.BACKLOG, userId, 4 - i, TutorialMessage.BACKLOG_MESSAGES.get(i));
        }
    }
    private void createAndSaveTodo(Type type, Long userId, int order, String tutorialMessage) {
        Todo todo = null;
        if (type.equals(Type.TODAY)) {
            todo = Todo.builder()
                    .userId(userId)
                    .type(type)
                    .content(tutorialMessage)
                    .todayDate(LocalDate.now())
                    .todayStatus(TodayStatus.COMPLETED)
                    .todayOrder(order)
                    .build();
        }
        if (type.equals(Type.BACKLOG)) {
            todo = Todo.builder()
                    .userId(userId)
                    .type(type)
                    .content(tutorialMessage)
                    .backlogOrder(order)
                    .build();
        }
        todoRepository.save(todo);
    }

    private LoginResponseDto createOldUserResponse(User existingUser, SocialUserInfo userInfo) {

        if (existingUser.getImageUrl() == null || existingUser.getImageUrl().isEmpty()) {
            existingUser.updateImageUrl(userInfo.imageUrl());
            userRepository.save(existingUser);
        }
        return createLoginResponse(existingUser, false);
    }

    private LoginResponseDto createLoginResponse(User user, boolean isNewUser) {
        TokenPair tokenPair = jwtService.generateTokenPair(String.valueOf(user.getId()));
        return AuthDtoConverter.toLoginDto(tokenPair, user, isNewUser);
    }

    public void logout(final Long userId) {
        userValidator.checkIsExistUser(userId);
        jwtService.deleteRefreshToken(String.valueOf(userId));
    }

    public TokenPair refresh(final ReissueTokenRequestDto reissueTokenRequestDto) {
        checkIsValidToken(reissueTokenRequestDto.getRefreshToken());

        final String userId = jwtService.getUserIdInToken(reissueTokenRequestDto.getRefreshToken());
        userValidator.checkIsExistUser(Long.parseLong(userId));

        final TokenPair tokenPair = jwtService.generateTokenPair(userId);
        jwtService.saveRefreshToken(userId, tokenPair.refreshToken());

        return tokenPair;
    }

    private void checkIsValidToken(String refreshToken) {
        try {
            jwtService.verifyToken(refreshToken);
            jwtService.compareRefreshToken(jwtService.getUserIdInToken(refreshToken), refreshToken);
        } catch (Exception e) {
            throw e;
        }
    }
}
