package chat.halixe.chat.data.repository;

import chat.halixe.chat.data.enums.ConversationType;
import chat.halixe.chat.data.model.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {


    Page<Conversation> findByorganisationId(
            UUID organisationId,
            Pageable pageable
    );

    Page<Conversation> findByorganisationIdAndType(
            UUID organisationId,
            ConversationType type,
            Pageable pageable
    );

    Optional<Conversation> findByIdAndOrganisationId(
            UUID conversationId,
            UUID organisationId
    );

    boolean existsByIdAndOrganisationId(
            UUID conversationId,
            UUID organisationId
    );

    @Query("""
            SELECT c FROM Conversation c
            WHERE c.organisationId = :orgId
              AND c.id IN :ids
            """)
    Page<Conversation> findByOrganisationIdAndIdIn(
            @Param("orgId") UUID orgId,
            @Param("ids") Collection<UUID> ids,
            Pageable pageable
    );
}
