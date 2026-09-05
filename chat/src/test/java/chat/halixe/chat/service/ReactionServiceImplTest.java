package chat.halixe.chat.service;

import chat.halixe.chat.data.model.Message;
import chat.halixe.chat.data.model.MessageReaction;
import chat.halixe.chat.data.repository.MessageReactionRepository;
import chat.halixe.chat.data.repository.MessageRepository;
import chat.halixe.chat.dtos.response.ReactionSummaryResponse;
import chat.halixe.chat.exception.ConversationNotFoundException;
import chat.halixe.chat.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class ReactionServiceImplTest {

    @Mock private MessageReactionRepository reactionRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks private ReactionServiceImpl service;

    private static final UUID MSG_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID ORG_ID = UUID.randomUUID();

    private AuthenticatedUser caller() {
        return new AuthenticatedUser(USER_ID, ORG_ID);
    }

    // ── toggleReaction ─────────────────────────────────────────────────────

    @Test
    void toggleReactionAddsReactionWhenNotPresent() {
        when(messageRepository.findById(MSG_ID)).thenReturn(Optional.of(new Message()));
        when(reactionRepository.existsByMessageIdAndUserIdAndEmoji(MSG_ID, USER_ID, "👍")).thenReturn(false);
        when(reactionRepository.findByMessageId(MSG_ID)).thenReturn(List.of(
                MessageReaction.builder().id(UUID.randomUUID()).messageId(MSG_ID).userId(USER_ID).emoji("👍").build()
        ));

        List<ReactionSummaryResponse> result = service.toggleReaction(MSG_ID, "👍", caller());

        verify(reactionRepository).save(any());
        verify(reactionRepository, never()).deleteByMessageIdAndUserIdAndEmoji(any(), any(), any());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmoji()).isEqualTo("👍");
        assertThat(result.get(0).getCount()).isEqualTo(1);
    }

    @Test
    void toggleReactionRemovesReactionWhenAlreadyPresent() {
        when(messageRepository.findById(MSG_ID)).thenReturn(Optional.of(new Message()));
        when(reactionRepository.existsByMessageIdAndUserIdAndEmoji(MSG_ID, USER_ID, "👍")).thenReturn(true);
        when(reactionRepository.findByMessageId(MSG_ID)).thenReturn(List.of());

        List<ReactionSummaryResponse> result = service.toggleReaction(MSG_ID, "👍", caller());

        verify(reactionRepository).deleteByMessageIdAndUserIdAndEmoji(MSG_ID, USER_ID, "👍");
        verify(reactionRepository, never()).save(any());
        assertThat(result).isEmpty();
    }

    @Test
    void toggleReactionBroadcastsToMessageTopic() {
        when(messageRepository.findById(MSG_ID)).thenReturn(Optional.of(new Message()));
        when(reactionRepository.existsByMessageIdAndUserIdAndEmoji(any(), any(), any())).thenReturn(false);
        when(reactionRepository.findByMessageId(MSG_ID)).thenReturn(List.of());

        service.toggleReaction(MSG_ID, "❤️", caller());

        verify(messagingTemplate).convertAndSend(
                eq("/topic/messages/" + MSG_ID + "/reactions"),
                any(Object.class)
        );
    }

    @Test
    void toggleReactionThrowsWhenMessageNotFound() {
        when(messageRepository.findById(MSG_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.toggleReaction(MSG_ID, "👍", caller()))
                .isInstanceOf(ConversationNotFoundException.class);

        verify(reactionRepository, never()).save(any());
    }

    // ── getReactions ───────────────────────────────────────────────────────

    @Test
    void getReactionsGroupsByEmoji() {
        UUID other = UUID.randomUUID();
        when(messageRepository.findById(MSG_ID)).thenReturn(Optional.of(new Message()));
        when(reactionRepository.findByMessageId(MSG_ID)).thenReturn(List.of(
                MessageReaction.builder().id(UUID.randomUUID()).messageId(MSG_ID).userId(USER_ID).emoji("🔥").build(),
                MessageReaction.builder().id(UUID.randomUUID()).messageId(MSG_ID).userId(other).emoji("🔥").build()
        ));

        List<ReactionSummaryResponse> result = service.getReactions(MSG_ID, caller());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCount()).isEqualTo(2);
        assertThat(result.get(0).isReactedByRequester()).isTrue();
    }

    @Test
    void getReactionsThrowsWhenMessageNotFound() {
        when(messageRepository.findById(MSG_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getReactions(MSG_ID, caller()))
                .isInstanceOf(ConversationNotFoundException.class);
    }
}
