package chat.halixe.chat.data.repository;

import chat.halixe.chat.data.model.PinnedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PinnedMessageRepository extends JpaRepository<PinnedMessage, UUID> {

    List<PinnedMessage> findByConversationIdOrderByPinnedAtDesc(UUID conversationId);

    boolean existsByConversationIdAndMessageId(
            UUID conversationId,
            UUID messageId
    );

    void deleteByConversationIdAndMessageId(
            UUID conversationId,
            UUID messageId
    );

    long countByConversationId(UUID conversationId);
}