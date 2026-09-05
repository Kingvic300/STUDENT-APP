package chat.halixe.chat.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionSummaryResponse {

    private String emoji;
    private long count;
    private boolean reactedByRequester;
}