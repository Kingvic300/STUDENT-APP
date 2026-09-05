package chat.halixe.chat.service;

import chat.halixe.chat.security.AuthenticatedUser;

import java.util.UUID;

public interface MessageReadService {

    void markAsRead(UUID conversationId, UUID messageId, AuthenticatedUser caller);

    long countUnread(UUID conversationId, AuthenticatedUser caller);
}
