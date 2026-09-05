package chat.halixe.chat.dtos.request;


import lombok.*;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ListThreadRepliesRequest {

    private UUID threadId;
    private Pageable pageable;

}
