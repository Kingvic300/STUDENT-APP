package chat.halixe.chat.dtos.request;

import chat.halixe.chat.data.enums.ConversationType;
import chat.halixe.chat.data.enums.ConversationVisibility;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class CreateConversationRequest {

    private String name;
    private ConversationType type;
    private ConversationVisibility visibility;
    private List<UUID> membersId;
}
