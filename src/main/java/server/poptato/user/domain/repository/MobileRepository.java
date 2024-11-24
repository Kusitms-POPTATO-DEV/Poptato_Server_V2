package server.poptato.user.domain.repository;

import server.poptato.user.domain.entity.Mobile;

public interface MobileRepository {
    Mobile save(Mobile mobile);
    void deleteAllByUserId(Long userId);
}
