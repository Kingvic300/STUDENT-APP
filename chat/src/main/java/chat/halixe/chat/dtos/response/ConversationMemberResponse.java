package chat.halixe.chat.dtos.response;

import chat.halixe.chat.data.enums.ConversationRole;
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
public class ConversationMemberResponse {

    private UUID id;
    private UUID conversationId;
    private UUID userId;
    private ConversationRole role;
    private Instant joinedAt;
    private Instant leftAt;
    private boolean muted;
    private UUID lastReadMessageId;
    private String displayName;
    private String avatarUrl;
}