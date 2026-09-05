package chat.halixe.chat.service;

import chat.halixe.chat.data.enums.PresentStatus;
import chat.halixe.chat.dtos.response.PresenceResponse;
import chat.halixe.chat.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PresenceServiceImplTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private ValueOperations<String, String> valueOps;

    @InjectMocks private PresenceServiceImpl service;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID ORG_ID = UUID.randomUUID();
    private static final String KEY = "presence:" + USER_ID;

    private AuthenticatedUser caller() {
        return new AuthenticatedUser(USER_ID, ORG_ID);
    }

    // ── updatePresence ─────────────────────────────────────────────────────

    @Test
    void updatePresenceOnlineStoresInRedisAndBroadcasts() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        service.updatePresence(PresentStatus.ONLINE, caller());

        verify(valueOps).set(eq(KEY), eq("ONLINE"), any(Duration.class));
        verify(redisTemplate, never()).delete(KEY);
        verify(messagingTemplate).convertAndSend(
                eq("/topic/presence/" + USER_ID), any(Object.class)
        );
    }

    @Test
    void updatePresenceOfflineDeletesKeyAndBroadcasts() {
        service.updatePresence(PresentStatus.OFFLINE, caller());

        verify(redisTemplate).delete(KEY);
        verify(redisTemplate, never()).opsForValue();
        verify(messagingTemplate).convertAndSend(
                eq("/topic/presence/" + USER_ID), any(Object.class)
        );
    }

    @Test
    void updatePresenceAwayStoresAwayStatus() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        service.updatePresence(PresentStatus.AWAY, caller());

        verify(valueOps).set(eq(KEY), eq("AWAY"), any(Duration.class));
    }

    // ── getPresence ────────────────────────────────────────────────────────

    @Test
    void getPresenceReturnsOnlineWhenKeyExists() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(KEY)).thenReturn("ONLINE");

        PresenceResponse response = service.getPresence(USER_ID);

        assertThat(response.getUserId()).isEqualTo(USER_ID);
        assertThat(response.getStatus()).isEqualTo(PresentStatus.ONLINE);
    }

    @Test
    void getPresenceReturnsOfflineWhenKeyAbsent() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(KEY)).thenReturn(null);

        PresenceResponse response = service.getPresence(USER_ID);

        assertThat(response.getStatus()).isEqualTo(PresentStatus.OFFLINE);
    }

    // ── setOffline ─────────────────────────────────────────────────────────

    @Test
    void setOfflineDeletesKeyAndBroadcastsOffline() {
        service.setOffline(USER_ID);

        verify(redisTemplate).delete(KEY);
        verify(messagingTemplate).convertAndSend(
                eq("/topic/presence/" + USER_ID), any(Object.class)
        );
    }
}
