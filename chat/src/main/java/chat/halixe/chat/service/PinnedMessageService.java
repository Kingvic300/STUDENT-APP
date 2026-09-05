package chat.halixe.chat.service;

import chat.halixe.chat.dtos.response.PinnedMessageResponse;
import chat.halixe.chat.security.AuthenticatedUser;

import java.util.List;
import java.util.UUID;

public interface PinnedMessageService {

    PinnedMessageResponse pinMessage(UUID conversationId, UUID messageId, AuthenticatedUser caller);

    void unpinMessage(UUID conversationId, UUID messageId, AuthenticatedUser caller);

    List<PinnedMessageResponse> listPinned(UUID conversationId, AuthenticatedUser caller);
}
