package server.poptato.todo.application.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class TodoDetailResponseDto {
    String content;
    LocalDate deadline;
    Boolean isBookmark;
}
