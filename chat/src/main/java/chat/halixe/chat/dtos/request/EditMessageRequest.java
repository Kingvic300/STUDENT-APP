package chat.halixe.chat.dtos.request;


import lombok.*;

import java.util.UUID;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EditMessageRequest {

    private String content;
    private UUID messageId;
    private UUID requesterId;
}
