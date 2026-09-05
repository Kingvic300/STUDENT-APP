package chat.halixe.chat.dtos.response;

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
public class HuddleResponse {
    private String callId;
    private UUID conversationId;
    private UUID initiatorId;
    private String status;
    private String joinToken;
}
