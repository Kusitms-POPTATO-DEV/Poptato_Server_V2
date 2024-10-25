package server.poptato.policy.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import server.poptato.global.response.BaseResponse;
import server.poptato.policy.application.PolicyService;
import server.poptato.policy.domain.entity.Policy;

@RestController
@RequiredArgsConstructor
public class PolicyController {
    private final PolicyService policyService;

    @GetMapping("/policy")
    public BaseResponse<Policy> getTodayList(){
        Policy policy = policyService.getPrivacyPolicy();
        return new BaseResponse<>(policy);
    }
}
