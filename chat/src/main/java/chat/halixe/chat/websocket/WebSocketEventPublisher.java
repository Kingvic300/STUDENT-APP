package chat.halixe.chat.websocket;

import chat.halixe.chat.dtos.response.MessageResponse;
import chat.halixe.chat.event.ChatEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebSocketEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishMessageSent(MessageResponse message) {
        broadcast(message.getConversationId(), "MESSAGE_SENT", message);
    }

    public void publishMessageEdited(MessageResponse message) {
        broadcast(message.getConversationId(), "MESSAGE_EDITED", message);
    }

    public void publishMessageDeleted(MessageResponse message) {
        broadcast(message.getConversationId(), "MESSAGE_DELETED", message);
    }

    public void publishTyping(UUID conversationId, UUID userId) {
        messagingTemplate.convertAndSend(
                "/topic/conversations/" + conversationId + "/typing",
                new ChatEvent("TYPING", userId)
        );
    }

    private void broadcast(UUID conversationId, String eventType, Object payload) {
        messagingTemplate.convertAndSend(
                "/topic/conversations/" + conversationId,
                new ChatEvent(eventType, payload)
        );
    }
}
