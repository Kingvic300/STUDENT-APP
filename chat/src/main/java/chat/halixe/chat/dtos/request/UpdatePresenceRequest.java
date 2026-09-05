package chat.halixe.chat.dtos.request;

import chat.halixe.chat.data.enums.PresentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePresenceRequest {
    @NotNull
    private PresentStatus status;
}
