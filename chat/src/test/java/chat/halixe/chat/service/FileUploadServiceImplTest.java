package chat.halixe.chat.service;

import chat.halixe.chat.client.StorageServiceClient;
import chat.halixe.chat.dtos.response.StoredFileResponse;
import chat.halixe.chat.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileUploadServiceImplTest {

    @Mock private StorageServiceClient storageServiceClient;
    @InjectMocks private FileUploadServiceImpl service;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID ORG_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "maxFileSizeMb", 25L);
    }

    private AuthenticatedUser caller() {
        return new AuthenticatedUser(USER_ID, ORG_ID);
    }

    @Test
    void uploadDelegatesToStorageClientAndReturnsResponse() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);

        StoredFileResponse expected = new StoredFileResponse(
                UUID.randomUUID(), "file.pdf", "application/pdf", 1024L, "https://cdn.example.com/file.pdf"
        );
        when(storageServiceClient.upload(file, USER_ID)).thenReturn(expected);

        StoredFileResponse result = service.upload(file, caller());

        assertThat(result).isEqualTo(expected);
        verify(storageServiceClient).upload(file, USER_ID);
    }

    @Test
    void uploadThrowsWhenFileIsEmpty() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> service.upload(file, caller()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");

        verifyNoInteractions(storageServiceClient);
    }

    @Test
    void uploadThrowsWhenFileTooLarge() {
        long maxBytes = 25L * 1024 * 1024;
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(maxBytes + 1);

        assertThatThrownBy(() -> service.upload(file, caller()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("25MB");

        verifyNoInteractions(storageServiceClient);
    }

    @Test
    void uploadWrapsStorageClientException() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(512L);
        when(storageServiceClient.upload(file, USER_ID))
                .thenThrow(new RuntimeException("storage unavailable"));

        assertThatThrownBy(() -> service.upload(file, caller()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("upload failed");
    }
}
