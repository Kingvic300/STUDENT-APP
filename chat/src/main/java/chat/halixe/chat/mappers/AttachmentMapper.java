package chat.halixe.chat.mappers;

import chat.halixe.chat.data.model.MessageAttachment;
import chat.halixe.chat.dtos.response.MessageAttachmentResponse;

public class AttachmentMapper {

    public static MessageAttachmentResponse toResponse(MessageAttachment a) {
        return MessageAttachmentResponse.builder()
                .id(a.getId())
                .messageId(a.getMessageId())
                .fileId(a.getFileId())
                .fileName(a.getFileName())
                .mimeType(a.getMimeType())
                .size(a.getSize())
                .url(a.getUrl())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
