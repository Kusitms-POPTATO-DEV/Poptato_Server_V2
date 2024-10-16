package server.poptato.todo.application.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.value.TodayStatus;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class TodayResponseDtoTest {
    @DisplayName("오늘이 16일이고 마감이 20일이면 남은 일은 4일로 계산된다.")
    @Test
    void 마감기한이_정상적으로_계산된다() {
        //given
        Todo todo = Todo.builder()
                .id(1L)
                .content("Test Todo")
                .todayStatus(TodayStatus.COMPLETED)
                .isBookmark(false)
                .deadline(LocalDate.of(2024, 10, 20))
                .todayDate(LocalDate.of(2024, 10, 16)) // 현재 날짜
                .build();

        //when
        TodayResponseDto responseDto = new TodayResponseDto(todo);

        //then
        assertThat(responseDto.getDeadline()).isEqualTo(4);
    }

    @DisplayName("마감이 설정안되어있는 경우 NULL로 정상 응답된다.")
    @Test
    void 마감기한_null_응답() {
        //given
        Todo todo = Todo.builder()
                .id(1L)
                .content("Test Todo without deadline")
                .todayStatus(TodayStatus.INCOMPLETE)
                .isBookmark(false)
                .deadline(null)  // 마감 기한이 없음
                .todayDate(LocalDate.of(2024, 10, 16)) // 현재 날짜
                .build();

        //when
        TodayResponseDto responseDto = new TodayResponseDto(todo);

        //then
        assertThat(responseDto.getDeadline()).isNull();
    }
}