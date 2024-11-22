package server.poptato.deleteReason.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import server.poptato.deleteReason.domain.entity.DeleteReason;
import server.poptato.deleteReason.domain.repository.DeleteReasonRepository;

public interface JpaDeleteReasonRepository extends DeleteReasonRepository, JpaRepository<DeleteReason, Long> {

}
