package chat.halixe.chat.mappers;

import chat.halixe.chat.data.model.MessageReaction;
import chat.halixe.chat.dtos.response.ReactionSummaryResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReactionMapper {

    public static List<ReactionSummaryResponse> toSummary(List<MessageReaction> reactions, UUID callerId) {
        Map<String, List<MessageReaction>> byEmoji = reactions.stream()
                .collect(Collectors.groupingBy(MessageReaction::getEmoji));

        return byEmoji.entrySet().stream()
                .map(e -> ReactionSummaryResponse.builder()
                        .emoji(e.getKey())
                        .count(e.getValue().size())
                        .reactedByRequester(e.getValue().stream()
                                .anyMatch(r -> r.getUserId().equals(callerId)))
                        .build())
                .toList();
    }
}
