package chat.halixe.chat.utils;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MentionParser {

    // matches @<full-uuid>  e.g. @550e8400-e29b-41d4-a716-446655440000
    private static final Pattern UUID_MENTION = Pattern.compile(
            "@([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})"
    );

    public static Set<UUID> extractMentions(String content) {
        if (content == null || content.isBlank()) return Set.of();

        Set<UUID> mentions = new LinkedHashSet<>();
        Matcher matcher = UUID_MENTION.matcher(content);
        while (matcher.find()) {
            try {
                mentions.add(UUID.fromString(matcher.group(1)));
            } catch (IllegalArgumentException ignored) {}
        }
        return mentions;
    }
}
