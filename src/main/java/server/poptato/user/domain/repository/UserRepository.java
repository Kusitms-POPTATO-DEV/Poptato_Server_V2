package server.poptato.user.domain.repository;

import server.poptato.user.domain.entity.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByKakaoId(String kakaoId);
}