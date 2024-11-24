package server.poptato.user.domain.repository;

import server.poptato.user.domain.entity.Mobile;

import java.util.Optional;

public interface MobileRepository {
    Mobile save(Mobile mobile);
    void deleteAllByUserId(Long userId);
    Mobile findByUserId(Long userId);
    Optional<Mobile> findByUserIdAndClientId(Long userId, String clientId);
}
