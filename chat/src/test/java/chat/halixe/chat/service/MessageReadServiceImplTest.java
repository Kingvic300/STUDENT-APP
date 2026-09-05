package chat.halixe.chat.service;

import chat.halixe.chat.data.enums.MessageType;
import chat.halixe.chat.data.model.ConversationMember;
import chat.halixe.chat.data.model.Message;
import chat.halixe.chat.data.repository.ConversationMemberRepository;
import chat.halixe.chat.data.repository.MessageReadRepository;
import chat.halixe.chat.data.repository.MessageRepository;
import chat.halixe.chat.exception.ConversationNotFoundException;
import chat.halixe.chat.exception.ForbiddenException;
import chat.halixe.chat.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageReadServiceImplTest {

    @Mock private MessageReadRepository messageReadRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private ConversationMemberRepository memberRepository;

    @InjectMocks private MessageReadServiceImpl service;

    private static final UUID CONV_ID = UUID.randomUUID();
    private static final UUID MSG_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID ORG_ID = UUID.randomUUID();

    private AuthenticatedUser caller() {
        return new AuthenticatedUser(USER_ID, ORG_ID);
    }

    private Message buildMessage() {
        return Message.builder()
                .id(MSG_ID).conversationId(CONV_ID)
                .senderId(UUID.randomUUID()).type(MessageType.TEXT).build();
    }

    // ── markAsRead ─────────────────────────────────────────────────────────

    @Test
    void markAsReadSavesReadRecordAndUpdatesLastRead() {
        when(messageRepository.findByIdAndConversationId(MSG_ID, CONV_ID))
                .thenReturn(Optional.of(buildMessage()));
        when(memberRepository.existsByConversationIdAndUserIdAndLeftAtIsNull(CONV_ID, USER_ID))
                .thenReturn(true);
        when(messageReadRepository.existsByMessageIdAndUserId(MSG_ID, USER_ID)).thenReturn(false);

        service.markAsRead(CONV_ID, MSG_ID, caller());

        verify(messageReadRepository).save(any());
        verify(memberRepository).updateLastReadMessage(CONV_ID, USER_ID, MSG_ID);
    }

    @Test
    void markAsReadSkipsDuplicateReadRecord() {
        when(messageRepository.findByIdAndConversationId(MSG_ID, CONV_ID))
                .thenReturn(Optional.of(buildMessage()));
        when(memberRepository.existsByConversationIdAndUserIdAndLeftAtIsNull(CONV_ID, USER_ID))
                .thenReturn(true);
        when(messageReadRepository.existsByMessageIdAndUserId(MSG_ID, USER_ID)).thenReturn(true);

        service.markAsRead(CONV_ID, MSG_ID, caller());

        verify(messageReadRepository, never()).save(any());
        verify(memberRepository).updateLastReadMessage(CONV_ID, USER_ID, MSG_ID);
    }

    @Test
    void markAsReadThrowsWhenMessageNotFound() {
        when(messageRepository.findByIdAndConversationId(MSG_ID, CONV_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markAsRead(CONV_ID, MSG_ID, caller()))
                .isInstanceOf(ConversationNotFoundException.class);

        verifyNoInteractions(messageReadRepository);
    }

    @Test
    void markAsReadThrowsWhenCallerNotMember() {
        when(messageRepository.findByIdAndConversationId(MSG_ID, CONV_ID))
                .thenReturn(Optional.of(buildMessage()));
        when(memberRepository.existsByConversationIdAndUserIdAndLeftAtIsNull(CONV_ID, USER_ID))
                .thenReturn(false);

        assertThatThrownBy(() -> service.markAsRead(CONV_ID, MSG_ID, caller()))
                .isInstanceOf(ForbiddenException.class);

        verify(messageReadRepository, never()).save(any());
    }

    // ── countUnread ────────────────────────────────────────────────────────

    @Test
    void countUnreadReturnsZeroWhenNotMember() {
        when(memberRepository.findByConversationIdAndUserId(CONV_ID, USER_ID))
                .thenReturn(Optional.empty());

        long count = service.countUnread(CONV_ID, caller());

        assertThat(count).isZero();
    }

    @Test
    void countUnreadReturnsZeroWhenLastReadIsSet() {
        ConversationMember member = ConversationMember.builder()
                .conversationId(CONV_ID).userId(USER_ID).lastReadMessageId(MSG_ID).build();
        when(memberRepository.findByConversationIdAndUserId(CONV_ID, USER_ID))
                .thenReturn(Optional.of(member));

        long count = service.countUnread(CONV_ID, caller());

        assertThat(count).isZero();
    }
}
