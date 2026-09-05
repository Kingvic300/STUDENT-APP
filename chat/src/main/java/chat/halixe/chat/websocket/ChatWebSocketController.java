package chat.halixe.chat.websocket;

import chat.halixe.chat.event.TypingEvent;
import chat.halixe.chat.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final WebSocketEventPublisher eventPublisher;

    @MessageMapping("/chat.typing")
    public void handleTyping(
            @Payload TypingEvent event,
            Principal principal
    ) {
        if (!(principal instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth)) {
            return;
        }
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        eventPublisher.publishTyping(event.conversationId(), user.userId());
    }
}
