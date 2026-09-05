package chat.halixe.chat.dtos.response;

import chat.halixe.chat.data.enums.ConversationType;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {

    private UUID id;
    private ConversationType type;
    private String name;
    private UUID organisationId;
    private UUID createdBy;
    private Instant createdAt;
    private Instant updatedAt;
    private Long memberCount;
    private Long unreadCount;
    private MessageResponse  response;
}
