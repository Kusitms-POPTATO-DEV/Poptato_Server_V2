package server.poptato.deleteReason.domain.repository;

import server.poptato.deleteReason.domain.entity.DeleteReason;

import java.util.List;

public interface DeleteReasonRepository {
    DeleteReason save(DeleteReason deleteReason);
}
