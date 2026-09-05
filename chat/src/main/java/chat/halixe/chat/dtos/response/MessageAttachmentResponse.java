package chat.halixe.chat.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageAttachmentResponse {

    private UUID id;
    private UUID messageId;
    private UUID fileId;
    private String fileName;
    private String mimeType;
    private long size;
    private String url;
    private Instant createdAt;
}