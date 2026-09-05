package chat.halixe.chat.controller;

import chat.halixe.chat.dtos.response.HuddleResponse;
import chat.halixe.chat.security.AuthenticatedUser;
import chat.halixe.chat.service.HuddleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class HuddleController {

    private final HuddleService huddleService;

    @PostMapping("/api/v1/conversations/{conversationId}/huddle")
    public ResponseEntity<HuddleResponse> initiate(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(huddleService.initiateHuddle(conversationId, caller));
    }

    @PostMapping("/api/v1/huddles/{callId}/join")
    public ResponseEntity<HuddleResponse> join(
            @PathVariable String callId,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        return ResponseEntity.ok(huddleService.joinHuddle(callId, caller));
    }

    @PostMapping("/api/v1/huddles/{callId}/decline")
    public ResponseEntity<Void> decline(
            @PathVariable String callId,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        huddleService.declineHuddle(callId, caller);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/v1/huddles/{callId}")
    public ResponseEntity<Void> end(
            @PathVariable String callId,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        huddleService.endHuddle(callId, caller);
        return ResponseEntity.noContent().build();
    }
}
