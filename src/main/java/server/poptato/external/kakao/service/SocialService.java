package server.poptato.external.kakao.service;


public abstract class SocialService {
    public abstract String getIdFromKakao(String baseUrl, String kakaoCode);

}