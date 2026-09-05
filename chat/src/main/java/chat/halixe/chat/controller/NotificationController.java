package chat.halixe.chat.controller;

import chat.halixe.chat.dtos.response.NotificationResponse;
import chat.halixe.chat.security.AuthenticatedUser;
import chat.halixe.chat.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        return ResponseEntity.ok(notificationService.getNotifications(page, size, caller));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> countUnread(
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        return ResponseEntity.ok(Map.of("count", notificationService.countUnread(caller)));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead(
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        notificationService.markAllAsRead(caller);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markRead(
            @PathVariable UUID notificationId,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        notificationService.markAsRead(notificationId, caller);
        return ResponseEntity.noContent().build();
    }
}
