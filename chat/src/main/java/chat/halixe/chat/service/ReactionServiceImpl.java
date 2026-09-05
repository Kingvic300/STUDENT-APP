package chat.halixe.chat.service;

import chat.halixe.chat.data.model.MessageReaction;
import chat.halixe.chat.data.repository.MessageReactionRepository;
import chat.halixe.chat.data.repository.MessageRepository;
import chat.halixe.chat.dtos.response.ReactionSummaryResponse;
import chat.halixe.chat.event.ChatEvent;
import chat.halixe.chat.exception.ConversationNotFoundException;
import chat.halixe.chat.mappers.ReactionMapper;
import chat.halixe.chat.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReactionServiceImpl implements ReactionService {

    private final MessageReactionRepository reactionRepository;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public List<ReactionSummaryResponse> toggleReaction(UUID messageId, String emoji, AuthenticatedUser caller) {
        messageRepository.findById(messageId)
                .orElseThrow(() -> new ConversationNotFoundException("Message not found: " + messageId));

        if (reactionRepository.existsByMessageIdAndUserIdAndEmoji(messageId, caller.userId(), emoji)) {
            reactionRepository.deleteByMessageIdAndUserIdAndEmoji(messageId, caller.userId(), emoji);
        } else {
            reactionRepository.save(MessageReaction.builder()
                    .messageId(messageId)
                    .userId(caller.userId())
                    .emoji(emoji)
                    .build());
        }

        List<ReactionSummaryResponse> reactions = buildSummary(messageId, caller.userId());

        messagingTemplate.convertAndSend(
                "/topic/messages/" + messageId + "/reactions",
                new ChatEvent("REACTION_UPDATED", reactions)
        );

        return reactions;
    }

    @Override
    public List<ReactionSummaryResponse> getReactions(UUID messageId, AuthenticatedUser caller) {
        messageRepository.findById(messageId)
                .orElseThrow(() -> new ConversationNotFoundException("Message not found: " + messageId));
        return buildSummary(messageId, caller.userId());
    }

    private List<ReactionSummaryResponse> buildSummary(UUID messageId, UUID callerId) {
        return ReactionMapper.toSummary(reactionRepository.findByMessageId(messageId), callerId);
    }
}
