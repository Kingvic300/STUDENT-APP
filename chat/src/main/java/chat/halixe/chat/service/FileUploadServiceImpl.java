package chat.halixe.chat.service;

import chat.halixe.chat.client.StorageServiceClient;
import chat.halixe.chat.dtos.response.StoredFileResponse;
import chat.halixe.chat.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    private final StorageServiceClient storageServiceClient;

    @Value("${storage-service.max-file-size-mb:25}")
    private long maxFileSizeMb;

    @Override
    public StoredFileResponse upload(MultipartFile file, AuthenticatedUser caller) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        long maxBytes = maxFileSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException("File exceeds maximum allowed size of " + maxFileSizeMb + "MB");
        }

        try {
            return storageServiceClient.upload(file, caller.userId());
        } catch (Exception e) {
            log.error("Failed to upload file to storage service", e);
            throw new RuntimeException("File upload failed: " + e.getMessage(), e);
        }
    }
}
