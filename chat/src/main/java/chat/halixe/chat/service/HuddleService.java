package chat.halixe.chat.service;

import chat.halixe.chat.dtos.response.HuddleResponse;
import chat.halixe.chat.security.AuthenticatedUser;

import java.util.UUID;

public interface HuddleService {

    HuddleResponse initiateHuddle(UUID conversationId, AuthenticatedUser caller);

    HuddleResponse joinHuddle(String callId, AuthenticatedUser caller);

    void declineHuddle(String callId, AuthenticatedUser caller);

    void endHuddle(String callId, AuthenticatedUser caller);
}
