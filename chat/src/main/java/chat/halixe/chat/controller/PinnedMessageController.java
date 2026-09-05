package chat.halixe.chat.controller;

import chat.halixe.chat.dtos.request.PinMessageRequest;
import chat.halixe.chat.dtos.response.PinnedMessageResponse;
import chat.halixe.chat.security.AuthenticatedUser;
import chat.halixe.chat.service.PinnedMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations/{conversationId}/pinned")
@RequiredArgsConstructor
public class PinnedMessageController {

    private final PinnedMessageService pinnedMessageService;

    @PostMapping
    public ResponseEntity<PinnedMessageResponse> pin(
            @PathVariable UUID conversationId,
            @Valid @RequestBody PinMessageRequest request,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pinnedMessageService.pinMessage(conversationId, request.getMessageId(), caller));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> unpin(
            @PathVariable UUID conversationId,
            @PathVariable UUID messageId,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        pinnedMessageService.unpinMessage(conversationId, messageId, caller);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<PinnedMessageResponse>> list(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        return ResponseEntity.ok(pinnedMessageService.listPinned(conversationId, caller));
    }
}
