package chat.halixe.chat.data.model;


import chat.halixe.chat.data.enums.ConversationType;
import chat.halixe.chat.data.enums.ConversationVisibility;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "conversations",
        schema = "chat" ,
        indexes = {
                @Index(name = "idx_conversation_org", columnList = "organisation_id"),
                @Index(name = "idx_conversation_type", columnList = "type")
        }
)
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ConversationType type;

    @Column(name = "visibility", nullable = false)
    @Enumerated(EnumType.STRING)
    private ConversationVisibility visibility;

    @Column(name = "organisation_id" , nullable = false )
    private UUID organisationId;

    private String name;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at" , nullable = false)
    private Instant updatedAt;




}
