package chat.halixe.chat.dtos.request;

import lombok.*;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ListMessageRequest {

    private UUID conversationId;

    private Pageable pageable;

    private boolean newestFirst;
}