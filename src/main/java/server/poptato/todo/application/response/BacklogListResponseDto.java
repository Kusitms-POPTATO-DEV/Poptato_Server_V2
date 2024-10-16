package server.poptato.todo.application.response;

import lombok.Builder;
import lombok.Getter;
import server.poptato.todo.domain.entity.Todo;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class BacklogListResponseDto {
    long totalCount;
    List<BacklogResponseDto> backlogs;
    int totalPageCount;

    @Builder
    public BacklogListResponseDto(long totalCount, List<Todo> backlogs, int totalPageCount) {
        this.totalCount = totalCount;
        this.backlogs = backlogs.stream()
                .map(BacklogResponseDto::new)
                .collect(Collectors.toList());
        this.totalPageCount = totalPageCount;
    }
}
