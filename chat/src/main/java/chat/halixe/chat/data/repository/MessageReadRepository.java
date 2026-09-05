package chat.halixe.chat.data.repository;

import chat.halixe.chat.data.model.MessageRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface MessageReadRepository extends JpaRepository<MessageRead, UUID> {


    List<MessageRead> findByMessageId(UUID messageId);

    boolean existsByMessageIdAndUserId(
            UUID messageId,
            UUID userId
    );

    long countByMessageId(UUID messageId);
}