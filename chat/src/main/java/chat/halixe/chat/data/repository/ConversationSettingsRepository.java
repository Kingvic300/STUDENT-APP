package chat.halixe.chat.data.repository;

import chat.halixe.chat.data.model.ConversationSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface ConversationSettingsRepository extends JpaRepository<ConversationSettings, UUID> {

    Optional<ConversationSettings> findByConversationId(UUID conversationId);

    boolean existsByConversationId(UUID conversationId);
}