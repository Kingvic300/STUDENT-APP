package chat.halixe.chat.data.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversation_settings", schema = "chat")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "conversation_id", nullable = false, unique = true)
    private UUID conversationId;

    @Column(name = "allow_members_to_invite", nullable = false)
    @Builder.Default
    private boolean allowMembersToInvite = true;

    @Column(name = "allow_members_to_send", nullable = false)
    @Builder.Default
    private boolean allowMembersToSend = true;

    @Column(name = "allow_file_sharing", nullable = false)
    @Builder.Default
    private boolean allowFileSharing = true;

    @Column(name = "allow_reactions", nullable = false)
    @Builder.Default
    private boolean allowReactions = true;

    @Column(name = "message_retention_days")
    private Integer messageRetentionDays;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
