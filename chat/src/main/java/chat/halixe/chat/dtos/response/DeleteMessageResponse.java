package chat.halixe.chat.dtos.response;


import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeleteMessageResponse {
    private String message;
}
