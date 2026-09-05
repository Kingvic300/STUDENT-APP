package chat.halixe.chat.dtos.request;


import chat.halixe.chat.data.enums.ConversationRole;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class DemoteUserRequest {

    private UUID userId;
    private ConversationRole role;
}
