package chat.halixe.chat.event;

import java.util.UUID;

public record TypingEvent(UUID conversationId) {}
