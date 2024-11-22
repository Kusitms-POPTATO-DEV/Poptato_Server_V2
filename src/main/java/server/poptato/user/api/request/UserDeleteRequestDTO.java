package server.poptato.user.api.request;

import server.poptato.deleteReason.domain.value.Reason;

import java.util.List;

public record UserDeleteRequestDTO(List<Reason> reasons, String userInputReason) {
}
