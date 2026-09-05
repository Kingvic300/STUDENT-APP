package chat.halixe.chat.service;

import chat.halixe.chat.data.enums.PresentStatus;
import chat.halixe.chat.dtos.response.PresenceResponse;
import chat.halixe.chat.event.ChatEvent;
import chat.halixe.chat.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PresenceServiceImpl implements PresenceService {

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    private static final Duration PRESENCE_TTL = Duration.ofMinutes(5);
    private static final String KEY_PREFIX = "presence:";

    @Override
    public void updatePresence(PresentStatus status, AuthenticatedUser caller) {
        String key = KEY_PREFIX + caller.userId();

        if (status == PresentStatus.OFFLINE) {
            redisTemplate.delete(key);
        } else {
            redisTemplate.opsForValue().set(key, status.name(), PRESENCE_TTL);
        }

        PresenceResponse response = new PresenceResponse(caller.userId(), status);
        messagingTemplate.convertAndSend(
                "/topic/presence/" + caller.userId(),
                new ChatEvent("PRESENCE_UPDATED", response)
        );
    }

    @Override
    public PresenceResponse getPresence(UUID userId) {
        String raw = redisTemplate.opsForValue().get(KEY_PREFIX + userId);
        PresentStatus status = raw != null
                ? PresentStatus.valueOf(raw)
                : PresentStatus.OFFLINE;
        return new PresenceResponse(userId, status);
    }

    @Override
    public void setOffline(UUID userId) {
        redisTemplate.delete(KEY_PREFIX + userId);
        messagingTemplate.convertAndSend(
                "/topic/presence/" + userId,
                new ChatEvent("PRESENCE_UPDATED", new PresenceResponse(userId, PresentStatus.OFFLINE))
        );
    }
}
