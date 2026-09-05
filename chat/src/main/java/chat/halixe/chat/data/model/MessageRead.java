package chat.halixe.chat.data.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "message_reads",
        schema = "chat",
        indexes = {
                @Index(name = "idx_message_reads_message", columnList = "message_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_message_read_user", columnNames = {"message_id", "user_id"})
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRead {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "read_at", nullable = false)
    private Instant readAt;
}

