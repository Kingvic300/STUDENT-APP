package chat.halixe.chat.security;

import java.util.UUID;

public record AuthenticatedUser(UUID userId, UUID organisationId) {
    @Override
    public String toString() {
        return userId.toString();
    }
}
