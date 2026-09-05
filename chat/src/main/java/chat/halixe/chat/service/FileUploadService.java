package chat.halixe.chat.service;

import chat.halixe.chat.dtos.response.StoredFileResponse;
import chat.halixe.chat.security.AuthenticatedUser;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {

    StoredFileResponse upload(MultipartFile file, AuthenticatedUser caller);
}
