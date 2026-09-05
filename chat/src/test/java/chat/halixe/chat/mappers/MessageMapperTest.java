package chat.halixe.chat.mappers;

import chat.halixe.chat.data.enums.MessageType;
import chat.halixe.chat.data.model.Message;
import chat.halixe.chat.data.model.MessageAttachment;
import chat.halixe.chat.dtos.request.SendMessageRequest;
import chat.halixe.chat.dtos.response.MessageResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MessageMapperTest {

    @Test
    void mapToSendMessageCopiesAllFields() {
        UUID convId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID threadId = UUID.randomUUID();
        UUID replyTo = UUID.randomUUID();

        SendMessageRequest request = SendMessageRequest.builder()
                .conversationId(convId)
                .senderId(senderId)
                .content("hello")
                .messageType(MessageType.TEXT)
                .threadId(threadId)
                .replyToMessageId(replyTo)
                .build();

        Message message = MessageMapper.mapToSendMessage(request);

        assertThat(message.getConversationId()).isEqualTo(convId);
        assertThat(message.getSenderId()).isEqualTo(senderId);
        assertThat(message.getContent()).isEqualTo("hello");
        assertThat(message.getType()).isEqualTo(MessageType.TEXT);
        assertThat(message.getThreadId()).isEqualTo(threadId);
        assertThat(message.getReplyToMessageId()).isEqualTo(replyTo);
    }

    @Test
    void mapToSendMessageResponseIncludesAttachments() {
        Message message = buildMessage();
        MessageAttachment attachment = MessageAttachment.builder()
                .id(UUID.randomUUID())
                .messageId(message.getId())
                .fileId(UUID.randomUUID())
                .fileName("photo.png")
                .mimeType("image/png")
                .size(1024L)
                .url("https://cdn.example.com/photo.png")
                .build();

        MessageResponse response = MessageMapper.mapToSendMessageResponse(message, List.of(attachment));

        assertThat(response.getAttachments()).hasSize(1);
        assertThat(response.getAttachments().get(0).getFileName()).isEqualTo("photo.png");
        assertThat(response.getAttachments().get(0).getMimeType()).isEqualTo("image/png");
    }

    @Test
    void mapToSendMessageResponseWithNoAttachmentsReturnsEmptyList() {
        MessageResponse response = MessageMapper.mapToSendMessageResponse(buildMessage());
        assertThat(response.getAttachments()).isEmpty();
    }

    @Test
    void deletedFlagSetWhenDeletedAtIsNotNull() {
        Message message = buildMessage();
        message.setDeletedAt(Instant.now());

        MessageResponse response = MessageMapper.mapToSendMessageResponse(message);
        assertThat(response.isDeleted()).isTrue();
    }

    @Test
    void deletedFlagFalseWhenDeletedAtIsNull() {
        Message message = buildMessage();
        message.setDeletedAt(null);

        MessageResponse response = MessageMapper.mapToSendMessageResponse(message);
        assertThat(response.isDeleted()).isFalse();
    }

    @Test
    void editedAtIsPreservedInResponse() {
        Message message = buildMessage();
        Instant editedAt = Instant.now();
        message.setEditedAt(editedAt);

        MessageResponse response = MessageMapper.mapToSendMessageResponse(message);
        assertThat(response.getEditedAt()).isEqualTo(editedAt);
    }

    private Message buildMessage() {
        return Message.builder()
                .id(UUID.randomUUID())
                .conversationId(UUID.randomUUID())
                .senderId(UUID.randomUUID())
                .content("test content")
                .type(MessageType.TEXT)
                .build();
    }
}
