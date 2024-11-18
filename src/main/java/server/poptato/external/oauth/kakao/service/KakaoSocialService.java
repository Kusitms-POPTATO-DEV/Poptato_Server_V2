package server.poptato.external.oauth.kakao.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import server.poptato.external.oauth.SocialService;
import server.poptato.external.oauth.SocialUserInfo;
import server.poptato.external.oauth.kakao.dto.response.KakaoUserResponse;
import server.poptato.external.oauth.kakao.feign.KakaoApiClient;

@Service
@RequiredArgsConstructor
public class KakaoSocialService extends SocialService {
    private static final String Bearer = "Bearer ";
    private final KakaoApiClient kakaoApiClient;

    @Override
    public SocialUserInfo getUserData(String accessToken) {
        KakaoUserResponse userResponse = kakaoApiClient.getUserInformation(Bearer + accessToken);

        return new SocialUserInfo(
                String.valueOf(userResponse.id()),
                userResponse.properties().nickname(),
                userResponse.kakao_account().email(),
                userResponse.kakao_account().profile().profile_image_url()

        );
    }
}

