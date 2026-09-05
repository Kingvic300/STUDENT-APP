package chat.halixe.chat.dtos.request;

import chat.halixe.chat.data.enums.ConversationRole;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class AddConversationMemberRequest {

    private UUID userId;
    private UUID conversationId;
    private ConversationRole role;
}
