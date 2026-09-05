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
public class StoredFileResponse {
    private UUID fileId;
    private String fileName;
    private String mimeType;
    private Long size;
    private String url;
}
