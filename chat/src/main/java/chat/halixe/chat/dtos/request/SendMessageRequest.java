package chat.halixe.chat.dtos.request;


import chat.halixe.chat.data.enums.MessageType;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageRequest {

    private UUID conversationId;
    private UUID senderId;
    private String content;
    private MessageType messageType;
    private UUID threadId;
    private UUID replyToMessageId;
    private List<AttachmentRequest> attachments;
}
