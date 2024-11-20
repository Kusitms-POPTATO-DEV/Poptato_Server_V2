package server.poptato.todo.application.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record TodoDetailResponseDto(String content, LocalDate deadline, String categoryName, String emojiImageUrl, @JsonProperty("isBookmark") Boolean isBookmark, @JsonProperty("isRepeat") Boolean isRepeat) {
}