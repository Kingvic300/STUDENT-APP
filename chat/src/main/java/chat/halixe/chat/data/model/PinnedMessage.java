package chat.halixe.chat.data.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "pinned_messages",
        schema = "chat",
        indexes = {
                @Index(
                        name = "idx_pinned_conversations",
                        columnList = "conversation_id"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_pinned_conversation_message",
                        columnNames = {
                                "conversation_id",
                                "message_id"
                        }
                )
        }
)
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PinnedMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    @Column(name = "pinned_by", nullable = false)
    private UUID userId;

    @CreationTimestamp
    @Column(name = "pinned_at", nullable = false)
    private Instant pinnedAt;
}
