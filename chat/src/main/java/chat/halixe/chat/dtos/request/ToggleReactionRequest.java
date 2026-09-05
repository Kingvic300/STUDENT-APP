package chat.halixe.chat.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToggleReactionRequest {
    @NotBlank
    private String emoji;
}
