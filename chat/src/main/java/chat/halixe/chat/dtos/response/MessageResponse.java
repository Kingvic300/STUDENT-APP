package chat.halixe.chat.dtos.response;

import chat.halixe.chat.data.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private UUID id;
    private UUID conversationId;
    private UUID senderId;
    private String content;
    private MessageType type;
    private UUID replyToMessageId;
    private UUID threadId;
    private Instant createdAt;
    private Instant editedAt;
    private boolean deleted;
    private Instant updatedAt;
    private List<MessageAttachmentResponse> attachments;
    private List<ReactionSummaryResponse> reactions;
    private String senderDisplayName;
    private String senderAvatarUrl;
}