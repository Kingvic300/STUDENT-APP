package chat.halixe.chat.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class InitiateHuddleRequest {
    @NotNull
    private UUID conversationId;
}
