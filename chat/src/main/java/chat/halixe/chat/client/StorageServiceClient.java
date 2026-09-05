package chat.halixe.chat.client;

import chat.halixe.chat.dtos.response.StoredFileResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class StorageServiceClient {

    private final RestClient restClient;

    public StorageServiceClient(@Value("${storage-service.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public StoredFileResponse upload(MultipartFile file, UUID uploadedBy) throws IOException {
        String originalName = file.getOriginalFilename() != null
                ? file.getOriginalFilename()
                : "file";

        byte[] bytes = file.getBytes();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return originalName;
            }
        });
        body.add("uploadedBy", uploadedBy.toString());

        return restClient.post()
                .uri("/api/v1/files")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(StoredFileResponse.class);
    }

    public void delete(UUID fileId) {
        restClient.delete()
                .uri("/api/v1/files/{fileId}", fileId)
                .retrieve()
                .toBodilessEntity();
    }
}
