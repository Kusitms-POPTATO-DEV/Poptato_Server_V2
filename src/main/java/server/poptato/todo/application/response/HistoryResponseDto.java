package server.poptato.todo.application.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;


@Getter
@AllArgsConstructor
public class HistoryResponseDto {
    private Long todoId;
    private String content;
    private LocalDate date;
}