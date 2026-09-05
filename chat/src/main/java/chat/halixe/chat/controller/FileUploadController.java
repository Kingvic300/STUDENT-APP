package chat.halixe.chat.controller;

import chat.halixe.chat.dtos.response.StoredFileResponse;
import chat.halixe.chat.security.AuthenticatedUser;
import chat.halixe.chat.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoredFileResponse> upload(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        StoredFileResponse response = fileUploadService.upload(file, caller);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
