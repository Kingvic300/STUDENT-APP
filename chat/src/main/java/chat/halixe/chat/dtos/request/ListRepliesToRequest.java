package chat.halixe.chat.dtos.request;

import lombok.*;

import java.util.UUID;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ListRepliesToRequest {

    private UUID messageId;

}
