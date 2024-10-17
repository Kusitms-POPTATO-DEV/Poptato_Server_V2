package server.poptato.todo.api.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class DeadlineUpdateRequestDto {
    LocalDate deadline;
}
