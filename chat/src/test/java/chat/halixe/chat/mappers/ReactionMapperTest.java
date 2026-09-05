package chat.halixe.chat.mappers;

import chat.halixe.chat.data.model.MessageReaction;
import chat.halixe.chat.dtos.response.ReactionSummaryResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReactionMapperTest {

    private static final UUID CALLER_ID = UUID.randomUUID();

    @Test
    void emptyListReturnsEmptySummary() {
        assertThat(ReactionMapper.toSummary(List.of(), CALLER_ID)).isEmpty();
    }

    @Test
    void groupsByEmoji() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        List<MessageReaction> reactions = List.of(
                reaction(UUID.randomUUID(), user1, "👍"),
                reaction(UUID.randomUUID(), user2, "👍"),
                reaction(UUID.randomUUID(), user1, "❤️")
        );

        List<ReactionSummaryResponse> summary = ReactionMapper.toSummary(reactions, CALLER_ID);

        assertThat(summary).hasSize(2);
        ReactionSummaryResponse thumbs = summary.stream()
                .filter(r -> r.getEmoji().equals("👍")).findFirst().orElseThrow();
        assertThat(thumbs.getCount()).isEqualTo(2);
        assertThat(thumbs.isReactedByRequester()).isFalse();

        ReactionSummaryResponse heart = summary.stream()
                .filter(r -> r.getEmoji().equals("❤️")).findFirst().orElseThrow();
        assertThat(heart.getCount()).isEqualTo(1);
    }

    @Test
    void reactedByRequesterTrueWhenCallerIncluded() {
        List<MessageReaction> reactions = List.of(
                reaction(UUID.randomUUID(), CALLER_ID, "🔥"),
                reaction(UUID.randomUUID(), UUID.randomUUID(), "🔥")
        );

        List<ReactionSummaryResponse> summary = ReactionMapper.toSummary(reactions, CALLER_ID);

        assertThat(summary).hasSize(1);
        assertThat(summary.get(0).isReactedByRequester()).isTrue();
        assertThat(summary.get(0).getCount()).isEqualTo(2);
    }

    @Test
    void reactedByRequesterFalseWhenCallerNotIncluded() {
        List<MessageReaction> reactions = List.of(
                reaction(UUID.randomUUID(), UUID.randomUUID(), "😂")
        );

        List<ReactionSummaryResponse> summary = ReactionMapper.toSummary(reactions, CALLER_ID);

        assertThat(summary.get(0).isReactedByRequester()).isFalse();
    }

    private MessageReaction reaction(UUID id, UUID userId, String emoji) {
        return MessageReaction.builder()
                .id(id)
                .messageId(UUID.randomUUID())
                .userId(userId)
                .emoji(emoji)
                .build();
    }
}
