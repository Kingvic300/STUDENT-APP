package chat.halixe.chat.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AttachmentRequest {
    private UUID fileId;
    private String fileName;
    private String mimeType;
    private Long size;
    private String url;
}
