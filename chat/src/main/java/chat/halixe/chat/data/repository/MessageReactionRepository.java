package chat.halixe.chat.data.repository;

import chat.halixe.chat.data.model.MessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageReactionRepository extends JpaRepository<MessageReaction, UUID> {

    List<MessageReaction> findByMessageId(UUID messageId);

    List<MessageReaction> findByMessageIdAndUserId(
            UUID messageId,
            UUID userId
    );

    boolean existsByMessageIdAndUserIdAndEmoji(
            UUID messageId,
            UUID userId,
            String emoji
    );

    void deleteByMessageIdAndUserIdAndEmoji(
            UUID messageId,
            UUID userId,
            String emoji
    );

    long countByMessageIdAndEmoji(
            UUID messageId,
            String emoji
    );
}