package chat.halixe.chat.service;

import chat.halixe.chat.data.enums.MessageType;
import chat.halixe.chat.data.model.Message;
import chat.halixe.chat.data.model.Notification;
import chat.halixe.chat.data.repository.ConversationMemberRepository;
import chat.halixe.chat.data.repository.NotificationRepository;
import chat.halixe.chat.dtos.response.NotificationResponse;
import chat.halixe.chat.exception.ForbiddenException;
import chat.halixe.chat.exception.NotFoundException;
import chat.halixe.chat.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private ConversationMemberRepository memberRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks private NotificationServiceImpl service;

    private static final UUID CONV_ID = UUID.randomUUID();
    private static final UUID SENDER_ID = UUID.randomUUID();
    private static final UUID RECIPIENT_ID = UUID.randomUUID();
    private static final UUID MSG_ID = UUID.randomUUID();
    private static final UUID ORG_ID = UUID.randomUUID();

    private AuthenticatedUser caller(UUID userId) {
        return new AuthenticatedUser(userId, ORG_ID);
    }

    // ── processMentions ────────────────────────────────────────────────────

    @Test
    void processMentionsCreatesNotificationAndPushesEvent() {
        Message message = Message.builder()
                .id(MSG_ID)
                .conversationId(CONV_ID)
                .senderId(SENDER_ID)
                .content("Hey @" + RECIPIENT_ID + " check this out")
                .type(MessageType.TEXT)
                .build();

        when(memberRepository.existsByConversationIdAndUserIdAndLeftAtIsNull(CONV_ID, RECIPIENT_ID))
                .thenReturn(true);

        Notification savedNotification = Notification.builder()
                .id(UUID.randomUUID())
                .recipientId(RECIPIENT_ID)
                .actorId(SENDER_ID)
                .conversationId(CONV_ID)
                .messageId(MSG_ID)
                .type("MENTION")
                .build();
        when(notificationRepository.save(any())).thenReturn(savedNotification);

        service.processMentions(message);

        verify(notificationRepository).save(any());
        verify(messagingTemplate).convertAndSendToUser(
                eq(RECIPIENT_ID.toString()),
                eq("/queue/notifications"),
                any()
        );
    }

    @Test
    void processMentionsSkipsSender() {
        Message message = Message.builder()
                .id(MSG_ID)
                .conversationId(CONV_ID)
                .senderId(SENDER_ID)
                .content("Hey @" + SENDER_ID + " self-mention")
                .type(MessageType.TEXT)
                .build();

        service.processMentions(message);

        verify(notificationRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
    }

    @Test
    void processMentionsSkipsNonMembers() {
        Message message = Message.builder()
                .id(MSG_ID)
                .conversationId(CONV_ID)
                .senderId(SENDER_ID)
                .content("Hey @" + RECIPIENT_ID)
                .type(MessageType.TEXT)
                .build();

        when(memberRepository.existsByConversationIdAndUserIdAndLeftAtIsNull(CONV_ID, RECIPIENT_ID))
                .thenReturn(false);

        service.processMentions(message);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void processMentionsDoesNothingWhenContentIsNull() {
        Message message = Message.builder()
                .id(MSG_ID).conversationId(CONV_ID).senderId(SENDER_ID).content(null).build();

        service.processMentions(message);

        verifyNoInteractions(notificationRepository, messagingTemplate);
    }

    @Test
    void processMentionsDoesNothingWhenNoMentions() {
        Message message = Message.builder()
                .id(MSG_ID).conversationId(CONV_ID).senderId(SENDER_ID)
                .content("no mentions here").build();

        service.processMentions(message);

        verifyNoInteractions(notificationRepository);
    }

    // ── getNotifications ───────────────────────────────────────────────────

    @Test
    void getNotificationsReturnsPagedResults() {
        Notification n = Notification.builder()
                .id(UUID.randomUUID()).recipientId(RECIPIENT_ID)
                .type("MENTION").build();
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(
                eq(RECIPIENT_ID), any())).thenReturn(new PageImpl<>(List.of(n)));

        Page<NotificationResponse> page = service.getNotifications(0, 10, caller(RECIPIENT_ID));

        assertThat(page.getContent()).hasSize(1);
    }

    // ── countUnread ────────────────────────────────────────────────────────

    @Test
    void countUnreadDelegatesToRepository() {
        when(notificationRepository.countByRecipientIdAndReadFalse(RECIPIENT_ID)).thenReturn(5L);

        long count = service.countUnread(caller(RECIPIENT_ID));

        assertThat(count).isEqualTo(5L);
    }

    // ── markAllAsRead ──────────────────────────────────────────────────────

    @Test
    void markAllAsReadCallsRepository() {
        service.markAllAsRead(caller(RECIPIENT_ID));

        verify(notificationRepository).markAllReadByRecipientId(RECIPIENT_ID);
    }

    // ── markAsRead ─────────────────────────────────────────────────────────

    @Test
    void markAsReadSetsReadFlag() {
        UUID notifId = UUID.randomUUID();
        Notification notification = Notification.builder()
                .id(notifId).recipientId(RECIPIENT_ID).read(false).build();

        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any())).thenReturn(notification);

        service.markAsRead(notifId, caller(RECIPIENT_ID));

        assertThat(notification.isRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsReadThrowsWhenNotFound() {
        UUID notifId = UUID.randomUUID();
        when(notificationRepository.findById(notifId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markAsRead(notifId, caller(RECIPIENT_ID)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void markAsReadThrowsWhenNotRecipient() {
        UUID notifId = UUID.randomUUID();
        Notification notification = Notification.builder()
                .id(notifId).recipientId(UUID.randomUUID()).read(false).build();

        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> service.markAsRead(notifId, caller(RECIPIENT_ID)))
                .isInstanceOf(ForbiddenException.class);
    }
}
