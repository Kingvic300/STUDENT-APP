package chat.halixe.chat.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private UUID id;
    private UUID recipientId;
    private UUID actorId;
    private UUID conversationId;
    private UUID messageId;
    private String type;
    private boolean read;
    private Instant createdAt;
}
