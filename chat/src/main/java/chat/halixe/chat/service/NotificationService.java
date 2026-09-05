package chat.halixe.chat.service;

import chat.halixe.chat.data.model.Message;
import chat.halixe.chat.dtos.response.NotificationResponse;
import chat.halixe.chat.security.AuthenticatedUser;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface NotificationService {

    void processMentions(Message message);

    Page<NotificationResponse> getNotifications(int page, int size, AuthenticatedUser caller);

    long countUnread(AuthenticatedUser caller);

    void markAllAsRead(AuthenticatedUser caller);

    void markAsRead(UUID notificationId, AuthenticatedUser caller);
}
