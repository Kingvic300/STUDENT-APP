package chat.halixe.chat.mappers;

import chat.halixe.chat.data.enums.ConversationRole;
import chat.halixe.chat.data.enums.ConversationType;
import chat.halixe.chat.data.enums.ConversationVisibility;
import chat.halixe.chat.data.model.Conversation;
import chat.halixe.chat.data.model.ConversationMember;
import chat.halixe.chat.dtos.response.ConversationMemberResponse;
import chat.halixe.chat.dtos.response.ConversationResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationMapperTest {

    @Test
    void toResponseMapsAllFields() {
        UUID id = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        Instant now = Instant.now();

        Conversation conversation = Conversation.builder()
                .id(id)
                .type(ConversationType.CHANNEL)
                .visibility(ConversationVisibility.PRIVATE)
                .organisationId(orgId)
                .name("general")
                .createdBy(createdBy)
                .createdAt(now)
                .updatedAt(now)
                .build();

        ConversationResponse response = ConversationMapper.toResponse(conversation, 5L);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getType()).isEqualTo(ConversationType.CHANNEL);
        assertThat(response.getName()).isEqualTo("general");
        assertThat(response.getOrganisationId()).isEqualTo(orgId);
        assertThat(response.getCreatedBy()).isEqualTo(createdBy);
        assertThat(response.getMemberCount()).isEqualTo(5L);
    }

    @Test
    void toMemberResponseMapsAllFields() {
        UUID memberId = UUID.randomUUID();
        UUID convId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID lastRead = UUID.randomUUID();
        Instant joinedAt = Instant.now();

        ConversationMember member = ConversationMember.builder()
                .id(memberId)
                .conversationId(convId)
                .userId(userId)
                .role(ConversationRole.ADMIN)
                .joinedAt(joinedAt)
                .muted(true)
                .lastReadMessageId(lastRead)
                .build();

        ConversationMemberResponse response = ConversationMapper.toMemberResponse(member);

        assertThat(response.getId()).isEqualTo(memberId);
        assertThat(response.getConversationId()).isEqualTo(convId);
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getRole()).isEqualTo(ConversationRole.ADMIN);
        assertThat(response.getJoinedAt()).isEqualTo(joinedAt);
        assertThat(response.isMuted()).isTrue();
        assertThat(response.getLastReadMessageId()).isEqualTo(lastRead);
    }
}
