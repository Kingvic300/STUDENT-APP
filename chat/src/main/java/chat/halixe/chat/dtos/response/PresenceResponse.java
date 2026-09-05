package chat.halixe.chat.dtos.response;

import chat.halixe.chat.data.enums.PresentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresenceResponse {
    private UUID userId;
    private PresentStatus status;
}
