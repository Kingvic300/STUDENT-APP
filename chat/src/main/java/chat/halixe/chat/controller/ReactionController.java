package chat.halixe.chat.controller;

import chat.halixe.chat.dtos.request.ToggleReactionRequest;
import chat.halixe.chat.dtos.response.ReactionSummaryResponse;
import chat.halixe.chat.security.AuthenticatedUser;
import chat.halixe.chat.service.ReactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/messages/{messageId}/reactions")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;

    @PostMapping
    public ResponseEntity<List<ReactionSummaryResponse>> toggle(
            @PathVariable UUID messageId,
            @Valid @RequestBody ToggleReactionRequest request,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        return ResponseEntity.ok(reactionService.toggleReaction(messageId, request.getEmoji(), caller));
    }

    @GetMapping
    public ResponseEntity<List<ReactionSummaryResponse>> list(
            @PathVariable UUID messageId,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        return ResponseEntity.ok(reactionService.getReactions(messageId, caller));
    }
}
