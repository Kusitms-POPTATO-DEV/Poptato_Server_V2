package server.poptato.external.kakao.service;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import server.poptato.external.kakao.dto.response.KakaoAccessTokenResponse;
import server.poptato.external.kakao.dto.response.KakaoUserInfo;
import server.poptato.external.kakao.dto.response.KakaoUserResponse;
import server.poptato.external.kakao.feign.KakaoApiClient;
import server.poptato.external.kakao.feign.KakaoAuthApiClient;

@Service
@RequiredArgsConstructor
public class KakaoSocialService extends SocialService {

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.baseUrl}")
    private String baseUrl;
    private static final String Bearer = "Bearer ";
    private static final String GRANT_TYPE = "authorization_code";
    private static final String KAKAO_ROUTER = "/login/oauth2/code/kakao";
    private final KakaoAuthApiClient kakaoAuthApiClient;
    private final KakaoApiClient kakaoApiClient;

    @Override
    public KakaoUserInfo getIdAndNickNameAndEmailFromKakao(String kakaoCode) {
        String redirectUrl = baseUrl + KAKAO_ROUTER;
        KakaoAccessTokenResponse tokenResponse = kakaoAuthApiClient.getOAuth2AccessToken(
                GRANT_TYPE,
                clientId,
                redirectUrl,
                kakaoCode
        );

        // 액세스 토큰으로 사용자 정보 요청
        KakaoUserResponse userResponse = kakaoApiClient.getUserInformation(Bearer + tokenResponse.accessToken());

        // ID와 닉네임을 함께 반환
        return new KakaoUserInfo(String.valueOf(userResponse.id()), userResponse.properties().nickname(), userResponse.kakao_account().email());
    }
}
