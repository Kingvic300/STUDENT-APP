package chat.halixe.chat.mappers;

import chat.halixe.chat.data.model.Notification;
import chat.halixe.chat.dtos.response.NotificationResponse;

public class NotificationMapper {

    public static NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .recipientId(n.getRecipientId())
                .actorId(n.getActorId())
                .conversationId(n.getConversationId())
                .messageId(n.getMessageId())
                .type(n.getType())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
