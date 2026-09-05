package chat.halixe.chat.dtos.request;

import lombok.*;

import java.util.UUID;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetMessageRequest {

    private UUID messageId;
    private UUID conversationId;
}
