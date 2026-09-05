package chat.halixe.chat.service;

import chat.halixe.chat.data.model.Message;
import chat.halixe.chat.data.model.Notification;
import chat.halixe.chat.data.repository.ConversationMemberRepository;
import chat.halixe.chat.data.repository.NotificationRepository;
import chat.halixe.chat.dtos.response.NotificationResponse;
import chat.halixe.chat.event.ChatEvent;
import chat.halixe.chat.exception.ForbiddenException;
import chat.halixe.chat.exception.NotFoundException;
import chat.halixe.chat.mappers.NotificationMapper;
import chat.halixe.chat.security.AuthenticatedUser;
import chat.halixe.chat.utils.MentionParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final ConversationMemberRepository memberRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void processMentions(Message message) {
        if (message.getContent() == null) return;

        Set<UUID> mentioned = MentionParser.extractMentions(message.getContent());
        if (mentioned.isEmpty()) return;

        mentioned.stream()
                .filter(userId -> !userId.equals(message.getSenderId()))
                .filter(userId -> memberRepository.existsByConversationIdAndUserIdAndLeftAtIsNull(
                        message.getConversationId(), userId))
                .forEach(userId -> {
                    Notification notification = notificationRepository.save(
                            Notification.builder()
                                    .recipientId(userId)
                                    .actorId(message.getSenderId())
                                    .conversationId(message.getConversationId())
                                    .messageId(message.getId())
                                    .type("MENTION")
                                    .build()
                    );

                    messagingTemplate.convertAndSendToUser(
                            userId.toString(),
                            "/queue/notifications",
                            new ChatEvent("MENTION", NotificationMapper.toResponse(notification))
                    );
                });
    }

    @Override
    public Page<NotificationResponse> getNotifications(int page, int size, AuthenticatedUser caller) {
        return notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(
                        caller.userId(),
                        PageRequest.of(page, size)
                )
                .map(NotificationMapper::toResponse);
    }

    @Override
    public long countUnread(AuthenticatedUser caller) {
        return notificationRepository.countByRecipientIdAndReadFalse(caller.userId());
    }

    @Override
    @Transactional
    public void markAllAsRead(AuthenticatedUser caller) {
        notificationRepository.markAllReadByRecipientId(caller.userId());
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId, AuthenticatedUser caller) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));

        if (!notification.getRecipientId().equals(caller.userId())) {
            throw new ForbiddenException("Not your notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }
}
