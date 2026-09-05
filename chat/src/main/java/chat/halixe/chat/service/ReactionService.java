package chat.halixe.chat.service;

import chat.halixe.chat.dtos.response.ReactionSummaryResponse;
import chat.halixe.chat.security.AuthenticatedUser;

import java.util.List;
import java.util.UUID;

public interface ReactionService {

    List<ReactionSummaryResponse> toggleReaction(UUID messageId, String emoji, AuthenticatedUser caller);

    List<ReactionSummaryResponse> getReactions(UUID messageId, AuthenticatedUser caller);
}
