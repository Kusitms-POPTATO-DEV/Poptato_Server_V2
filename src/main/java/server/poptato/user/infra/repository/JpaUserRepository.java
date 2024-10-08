package server.poptato.user.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import server.poptato.user.domain.entity.User;
import server.poptato.user.domain.repository.UserRepository;

import java.util.Optional;

public interface JpaUserRepository extends UserRepository, JpaRepository<User, Long> {
    @Override
    Optional<User> findByKakaoId(String kakaoId);
}