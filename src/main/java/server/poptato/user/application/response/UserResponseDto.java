package server.poptato.user.application.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponseDto {
    private String name;
    private String email;
}
