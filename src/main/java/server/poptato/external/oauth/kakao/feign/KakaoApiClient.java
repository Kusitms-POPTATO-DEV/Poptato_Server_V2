package server.poptato.external.oauth.kakao.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import server.poptato.external.oauth.kakao.dto.response.KakaoUserResponse;


@ComponentScan
@FeignClient(name = "kakaoApiClient", url = "https://kapi.kakao.com")
public interface KakaoApiClient {

    @GetMapping(value = "/v2/user/me")
    KakaoUserResponse getUserInformation(@RequestHeader(HttpHeaders.AUTHORIZATION) String accessToken);
}
