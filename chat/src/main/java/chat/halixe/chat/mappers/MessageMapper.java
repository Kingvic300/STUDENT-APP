package chat.halixe.chat.mappers;

import chat.halixe.chat.data.model.Message;
import chat.halixe.chat.data.model.MessageAttachment;
import chat.halixe.chat.dtos.request.DeleteMessageRequest;
import chat.halixe.chat.dtos.request.EditMessageRequest;
import chat.halixe.chat.dtos.request.SendMessageRequest;
import chat.halixe.chat.dtos.response.DeleteMessageResponse;
import chat.halixe.chat.dtos.response.MessageListResponse;
import chat.halixe.chat.dtos.response.MessageResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public class MessageMapper {

    public static Message mapToSendMessage(SendMessageRequest request) {
        Message message = new Message();
        message.setConversationId(request.getConversationId());
        message.setSenderId(request.getSenderId());
        message.setContent(request.getContent());
        message.setType(request.getMessageType());
        message.setThreadId(request.getThreadId());
        message.setReplyToMessageId(request.getReplyToMessageId());
        return message;
    }

    public static MessageResponse mapToSendMessageResponse(Message message) {
        return buildResponse(message, List.of());
    }

    public static MessageResponse mapToSendMessageResponse(Message message, List<MessageAttachment> attachments) {
        return buildResponse(message, attachments);
    }

    public static void mapToEditMessage(EditMessageRequest request, Message message) {
        message.setContent(request.getContent());
    }

    public static MessageResponse mapToEditMessageResponse(Message message) {
        return buildResponse(message, List.of());
    }

    public static void mapToDeleteMessage(DeleteMessageRequest request, Message message) {
    }

    public static DeleteMessageResponse mapToDeleteMessageResponse(String text) {
        DeleteMessageResponse response = new DeleteMessageResponse();
        response.setMessage(text);
        return response;
    }

    public static MessageResponse mapToGetMessageResponse(Message message, List<MessageAttachment> attachments) {
        return buildResponse(message, attachments);
    }

    public static MessageResponse mapToGetMessageResponse(Message message) {
        return buildResponse(message, List.of());
    }

    public static MessageListResponse mapToMessageListResponse(Page<Message> messagePage) {
        List<MessageResponse> messages = messagePage.getContent().stream()
                .map(m -> buildResponse(m, List.of()))
                .toList();

        return MessageListResponse.builder()
                .messages(messages)
                .page(messagePage.getNumber())
                .size(messagePage.getSize())
                .totalElements(messagePage.getTotalElements())
                .totalPages(messagePage.getTotalPages())
                .hasNext(messagePage.hasNext())
                .hasPrevious(messagePage.hasPrevious())
                .build();
    }

    private static MessageResponse buildResponse(Message message, List<MessageAttachment> attachments) {
        MessageResponse r = new MessageResponse();
        r.setId(message.getId());
        r.setConversationId(message.getConversationId());
        r.setSenderId(message.getSenderId());
        r.setContent(message.getContent());
        r.setType(message.getType());
        r.setReplyToMessageId(message.getReplyToMessageId());
        r.setThreadId(message.getThreadId());
        r.setCreatedAt(message.getCreatedAt());
        r.setUpdatedAt(message.getUpdatedAt());
        r.setEditedAt(message.getEditedAt());
        r.setDeleted(message.getDeletedAt() != null);
        r.setAttachments(attachments.stream().map(AttachmentMapper::toResponse).toList());
        return r;
    }
}
