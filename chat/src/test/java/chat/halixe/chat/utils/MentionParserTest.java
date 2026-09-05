package chat.halixe.chat.utils;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MentionParserTest {

    private static final UUID UUID_A = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final UUID UUID_B = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");

    @Test
    void returnsEmptySetForNullContent() {
        assertThat(MentionParser.extractMentions(null)).isEmpty();
    }

    @Test
    void returnsEmptySetForBlankContent() {
        assertThat(MentionParser.extractMentions("   ")).isEmpty();
    }

    @Test
    void returnsEmptySetWhenNoMentions() {
        assertThat(MentionParser.extractMentions("hello world, no mentions here")).isEmpty();
    }

    @Test
    void extractsSingleMention() {
        String content = "hey @" + UUID_A + " check this out";
        Set<UUID> result = MentionParser.extractMentions(content);
        assertThat(result).containsExactly(UUID_A);
    }

    @Test
    void extractsMultipleMentions() {
        String content = "@" + UUID_A + " and @" + UUID_B + " please review";
        Set<UUID> result = MentionParser.extractMentions(content);
        assertThat(result).containsExactlyInAnyOrder(UUID_A, UUID_B);
    }

    @Test
    void deduplicatesMentions() {
        String content = "@" + UUID_A + " hey @" + UUID_A + " again";
        Set<UUID> result = MentionParser.extractMentions(content);
        assertThat(result).hasSize(1).containsExactly(UUID_A);
    }

    @Test
    void ignoresInvalidUuidAfterAt() {
        String content = "@not-a-uuid hello @also-bad";
        assertThat(MentionParser.extractMentions(content)).isEmpty();
    }

    @Test
    void extractsMentionAtStartOfString() {
        String content = "@" + UUID_A + " is here";
        assertThat(MentionParser.extractMentions(content)).containsExactly(UUID_A);
    }

    @Test
    void extractsMentionAtEndOfString() {
        String content = "ping @" + UUID_A;
        assertThat(MentionParser.extractMentions(content)).containsExactly(UUID_A);
    }
}
