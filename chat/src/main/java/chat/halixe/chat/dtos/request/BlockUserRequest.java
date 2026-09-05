package chat.halixe.chat.dtos.request;


import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class BlockUserRequest {

    private UUID userId;
}
