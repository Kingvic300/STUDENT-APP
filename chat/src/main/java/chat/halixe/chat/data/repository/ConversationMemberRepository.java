package chat.halixe.chat.data.repository;

import chat.halixe.chat.data.model.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, UUID> {

    List<ConversationMember> findByConversationId(UUID conversationId);

    List<ConversationMember> findByConversationIdAndLeftAtIsNull(UUID conversationId);

    List<ConversationMember> findByUserId(UUID userId);

    Optional<ConversationMember> findByConversationIdAndUserId(
            UUID conversationId,
            UUID userId
    );

    boolean existsByConversationIdAndUserIdAndLeftAtIsNull(
            UUID conversationId,
            UUID userId
    );

    List<ConversationMember> findByUserIdAndLeftAtIsNull(UUID userId);

    long countByConversationIdAndLeftAtIsNull(UUID conversationId);

    @Modifying
    @Transactional
    @Query("""
            UPDATE ConversationMember m
            SET m.lastReadMessageId = :messageId
            WHERE m.conversationId = :conversationId
              AND m.userId = :userId
            """)
    int updateLastReadMessage(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId,
            @Param("messageId") UUID messageId
    );

    @Modifying
    @Transactional
    @Query("""
            UPDATE ConversationMember m
            SET m.muted = :muted
            WHERE m.conversationId = :conversationId
              AND m.userId = :userId
            """)
    int updateMuted(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId,
            @Param("muted") boolean muted
    );

    @Modifying
    @Transactional
    @Query("""
            UPDATE ConversationMember m
            SET m.leftAt = :leftAt
            WHERE m.conversationId = :conversationId
              AND m.userId = :userId
              AND m.leftAt IS NULL
            """)
    int markLeft(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId,
            @Param("leftAt") Instant leftAt
    );
}