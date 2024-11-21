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
    private String tutorialMessage = """
        ⭐️‘일단’ 이용 가이드⭐️

        1. ‘새로 추가하기…’를 눌러 할 일을 생성해보세요!
        2. •••을 눌러 할 일의 특성을 설정해보세요.
        3. 상단에 ⊕를 눌러 카테고리를 만들고 할 일을 관리해 보세요.
        4. 오늘 할 일을 왼쪽으로 옮겨보세요. 할 일이 ‘오늘’ 페이지로 이동해요.
        5. 오늘 할 일을 모두 체크해 보세요!✅

        다 읽었다면 •••을 눌러 삭제해도 좋습니다.
        ‘일단’은 당신의 하루를 응원합니다! 🙌
    """;

    public LoginResponseDto login(final KakaoLoginRequestDto loginRequestDto) {
        String accessToken = loginRequestDto.getKakaoCode();
        SocialPlatform socialPlatform = KAKAO;

        SocialService socialService = socialServiceProvider.getSocialService(socialPlatform);
        SocialUserInfo userInfo = socialService.getUserData(accessToken);
        Optional<User> user = userRepository.findByKakaoId(userInfo.socialId());
        if (user.isEmpty()) {
            LoginResponseDto response = createNewUserResponse(userInfo);
            createTutorial(response);
            return response;
        }
        return createOldUserResponse(user.get(), userInfo);
    }

    public void createTutorial(LoginResponseDto response) {
        Todo turorialTodo = Todo.createBacklog(response.userId(), tutorialMessage, 1);
        todoRepository.save(turorialTodo);
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
