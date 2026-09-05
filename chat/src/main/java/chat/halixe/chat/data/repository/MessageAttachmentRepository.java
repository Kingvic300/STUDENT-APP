package chat.halixe.chat.data.repository;

import chat.halixe.chat.data.model.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, UUID> {

    List<MessageAttachment> findByMessageId(UUID messageId);

    List<MessageAttachment> findByMessageIdIn(List<UUID> messageIds);

    long countByMessageId(UUID messageId);
}