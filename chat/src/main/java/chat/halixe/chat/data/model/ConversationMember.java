package chat.halixe.chat.data.model;


import chat.halixe.chat.data.enums.ConversationRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "conversation_member",
        schema = "chat" ,
        indexes = {
            @Index(name = "idx_conv_members_conversation", columnList = "conversation_id"),
            @Index(name = "idx_conv_members_user", columnList = "user_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_conversation_member",
                        columnNames = {
                            "conversation_id",
                            "user_id"
                        }
                )
        }

)
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Column( name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private ConversationRole role;

    @CreationTimestamp
    @Column( name= "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    @Column( name = "left_at")
    private Instant leftAt;

    @Column( name = "last_read_message_id")
    private UUID lastReadMessageId;

    private boolean muted = false;


}
