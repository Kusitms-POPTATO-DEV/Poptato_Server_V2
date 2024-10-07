package server.poptato.external.kakao.service;


import server.poptato.external.kakao.dto.response.KakaoUserInfo;

public abstract class SocialService {
    public abstract KakaoUserInfo getIdAndNickNameFromKakao(String baseUrl, String kakaoCode);

}