package chat.halixe.chat.service;

import chat.halixe.chat.data.enums.PresentStatus;
import chat.halixe.chat.dtos.response.PresenceResponse;
import chat.halixe.chat.security.AuthenticatedUser;

import java.util.UUID;

public interface PresenceService {

    void updatePresence(PresentStatus status, AuthenticatedUser caller);

    PresenceResponse getPresence(UUID userId);

    void setOffline(UUID userId);
}
