package server.poptato.todo.application.response;

import lombok.Getter;
import server.poptato.todo.domain.entity.Todo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
public class YesterdayResponseDto{
    Long todoId;
    Integer dDay;
    boolean isBookmark;
    String content;

    public YesterdayResponseDto(Todo todo) {
        this.todoId = todo.getId();
        this.content = todo.getContent();
        this.isBookmark = todo.isBookmark();

        if (hasDeadline(todo)) {
            this.dDay = (int) ChronoUnit.DAYS.between(LocalDate.now(), todo.getDeadline());
            return;
        }
        this.dDay = null;
    }

    private boolean hasDeadline(Todo todo) {
        return todo.getDeadline() != null && todo.getTodayDate() != null;
    }
}