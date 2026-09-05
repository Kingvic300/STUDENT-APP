package chat.halixe.chat.data.repository;

import chat.halixe.chat.data.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    Page<Message> findByConversationIdOrderByCreatedAtDesc(
            UUID conversationIds,
            Pageable pageable
    );

    Page<Message> findByConversationIdOrderByCreatedAtAsc(
            UUID conversationId,
            Pageable pageable
    );

    Page<Message> findBySenderIdOrderByCreatedAtDesc(
            UUID senderId,
            Pageable pageable
    );

    Page<Message> findByThreadIdOrderByCreatedAtAsc(
            UUID threadId,
            Pageable pageable
    );

    List<Message> findByReplyToMessageIdOrderByCreatedAtAsc(
            UUID replyToMessageId
    );

    List<Message> findTop50ByConversationIdOrderByCreatedAtDesc(
            UUID conversationId
    );

    boolean existsByIdAndConversationId(
            UUID messageId,
            UUID conversationId
    );

    java.util.Optional<Message> findByIdAndConversationId(
            UUID messageId,
            UUID conversationId
    );

}
