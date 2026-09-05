package chat.halixe.chat.service;

import chat.halixe.chat.data.enums.MessageType;
import chat.halixe.chat.data.model.Message;
import chat.halixe.chat.data.model.PinnedMessage;
import chat.halixe.chat.data.repository.ConversationMemberRepository;
import chat.halixe.chat.data.repository.ConversationRepository;
import chat.halixe.chat.data.repository.MessageRepository;
import chat.halixe.chat.data.repository.PinnedMessageRepository;
import chat.halixe.chat.dtos.response.PinnedMessageResponse;
import chat.halixe.chat.exception.ConversationNotFoundException;
import chat.halixe.chat.exception.ForbiddenException;
import chat.halixe.chat.exception.MemberAlreadyExistsException;
import chat.halixe.chat.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PinnedMessageServiceImplTest {

    @Mock private PinnedMessageRepository pinnedMessageRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private ConversationRepository conversationRepository;
    @Mock private ConversationMemberRepository memberRepository;

    @InjectMocks private PinnedMessageServiceImpl service;

    private static final UUID CONV_ID = UUID.randomUUID();
    private static final UUID ORG_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID MSG_ID = UUID.randomUUID();

    private AuthenticatedUser caller() {
        return new AuthenticatedUser(USER_ID, ORG_ID);
    }

    // ── pinMessage ─────────────────────────────────────────────────────────

    @Test
    void pinMessageSavesPinnedRecord() {
        when(conversationRepository.existsByIdAndOrganisationId(CONV_ID, ORG_ID)).thenReturn(true);
        when(memberRepository.existsByConversationIdAndUserIdAndLeftAtIsNull(CONV_ID, USER_ID)).thenReturn(true);
        when(messageRepository.existsByIdAndConversationId(MSG_ID, CONV_ID)).thenReturn(true);
        when(pinnedMessageRepository.existsByConversationIdAndMessageId(CONV_ID, MSG_ID)).thenReturn(false);

        PinnedMessage saved = PinnedMessage.builder()
                .id(UUID.randomUUID()).conversationId(CONV_ID).messageId(MSG_ID)
                .userId(USER_ID).pinnedAt(Instant.now()).build();
        when(pinnedMessageRepository.save(any())).thenReturn(saved);
        when(messageRepository.findById(MSG_ID)).thenReturn(Optional.of(
                Message.builder().id(MSG_ID).conversationId(CONV_ID)
                        .senderId(USER_ID).content("hi").type(MessageType.TEXT).build()
        ));

        PinnedMessageResponse response = service.pinMessage(CONV_ID, MSG_ID, caller());

        assertThat(response.getId()).isEqualTo(saved.getId());
        assertThat(response.getConversationId()).isEqualTo(CONV_ID);
        assertThat(response.getMessage()).isNotNull();
    }

    @Test
    void pinMessageThrowsWhenConversationNotFound() {
        when(conversationRepository.existsByIdAndOrganisationId(CONV_ID, ORG_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.pinMessage(CONV_ID, MSG_ID, caller()))
                .isInstanceOf(ConversationNotFoundException.class);
    }

    @Test
    void pinMessageThrowsWhenNotMember() {
        when(conversationRepository.existsByIdAndOrganisationId(CONV_ID, ORG_ID)).thenReturn(true);
        when(memberRepository.existsByConversationIdAndUserIdAndLeftAtIsNull(CONV_ID, USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.pinMessage(CONV_ID, MSG_ID, caller()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void pinMessageThrowsWhenMessageNotInConversation() {
        when(conversationRepository.existsByIdAndOrganisationId(CONV_ID, ORG_ID)).thenReturn(true);
        when(memberRepository.existsByConversationIdAndUserIdAndLeftAtIsNull(CONV_ID, USER_ID)).thenReturn(true);
        when(messageRepository.existsByIdAndConversationId(MSG_ID, CONV_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.pinMessage(CONV_ID, MSG_ID, caller()))
                .isInstanceOf(ConversationNotFoundException.class);
    }

    @Test
    void pinMessageThrowsWhenAlreadyPinned() {
        when(conversationRepository.existsByIdAndOrganisationId(CONV_ID, ORG_ID)).thenReturn(true);
        when(memberRepository.existsByConversationIdAndUserIdAndLeftAtIsNull(CONV_ID, USER_ID)).thenReturn(true);
        when(messageRepository.existsByIdAndConversationId(MSG_ID, CONV_ID)).thenReturn(true);
        when(pinnedMessageRepository.existsByConversationIdAndMessageId(CONV_ID, MSG_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.pinMessage(CONV_ID, MSG_ID, caller()))
                .isInstanceOf(MemberAlreadyExistsException.class);
    }

    // ── unpinMessage ───────────────────────────────────────────────────────

    @Test
    void unpinMessageDeletesRecord() {
        when(conversationRepository.existsByIdAndOrganisationId(CONV_ID, ORG_ID)).thenReturn(true);
        when(memberRepository.existsByConversationIdAndUserIdAndLeftAtIsNull(CONV_ID, USER_ID)).thenReturn(true);

        service.unpinMessage(CONV_ID, MSG_ID, caller());

        verify(pinnedMessageRepository).deleteByConversationIdAndMessageId(CONV_ID, MSG_ID);
    }

    // ── listPinned ─────────────────────────────────────────────────────────

    @Test
    void listPinnedReturnsAllPinnedMessages() {
        PinnedMessage pinned = PinnedMessage.builder()
                .id(UUID.randomUUID()).conversationId(CONV_ID).messageId(MSG_ID)
                .userId(USER_ID).pinnedAt(Instant.now()).build();

        when(conversationRepository.existsByIdAndOrganisationId(CONV_ID, ORG_ID)).thenReturn(true);
        when(memberRepository.existsByConversationIdAndUserIdAndLeftAtIsNull(CONV_ID, USER_ID)).thenReturn(true);
        when(pinnedMessageRepository.findByConversationIdOrderByPinnedAtDesc(CONV_ID))
                .thenReturn(List.of(pinned));
        when(messageRepository.findById(MSG_ID)).thenReturn(Optional.empty());

        List<PinnedMessageResponse> result = service.listPinned(CONV_ID, caller());

        assertThat(result).hasSize(1);
    }
}
