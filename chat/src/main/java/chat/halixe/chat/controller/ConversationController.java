package chat.halixe.chat.controller;

import chat.halixe.chat.dtos.request.AddConversationMemberRequest;
import chat.halixe.chat.dtos.request.CreateConversationRequest;
import chat.halixe.chat.dtos.response.ConversationMemberResponse;
import chat.halixe.chat.dtos.response.ConversationResponse;
import chat.halixe.chat.security.AuthenticatedUser;
import chat.halixe.chat.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping("/create-conversation")
    public ResponseEntity<ConversationResponse> create(
            @RequestBody CreateConversationRequest request,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(conversationService.createConversation(request, caller));
    }

    @GetMapping
    public ResponseEntity<Page<ConversationResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(conversationService.listConversations(pageable, caller));
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ConversationResponse> get(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        return ResponseEntity.ok(conversationService.getConversation(conversationId, caller));
    }

    @GetMapping("/{conversationId}/members")
    public ResponseEntity<List<ConversationMemberResponse>> listMembers(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        return ResponseEntity.ok(conversationService.listMembers(conversationId, caller));
    }

    @PostMapping("/{conversationId}/members")
    public ResponseEntity<ConversationMemberResponse> addMember(
            @PathVariable UUID conversationId,
            @RequestBody AddConversationMemberRequest request,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        request.setConversationId(conversationId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(conversationService.addMember(conversationId, request, caller));
    }

    @DeleteMapping("/{conversationId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID conversationId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        conversationService.removeMember(conversationId, userId, caller);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{conversationId}/leave")
    public ResponseEntity<Void> leave(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        conversationService.leaveConversation(conversationId, caller);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{conversationId}/members/{userId}/promote")
    public ResponseEntity<ConversationMemberResponse> promote(
            @PathVariable UUID conversationId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        return ResponseEntity.ok(conversationService.promoteMember(conversationId, userId, caller));
    }

    @PatchMapping("/{conversationId}/members/{userId}/demote")
    public ResponseEntity<ConversationMemberResponse> demote(
            @PathVariable UUID conversationId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        return ResponseEntity.ok(conversationService.demoteMember(conversationId, userId, caller));
    }

    @PatchMapping("/{conversationId}/mute")
    public ResponseEntity<Void> mute(
            @PathVariable UUID conversationId,
            @RequestParam boolean muted,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        conversationService.muteConversation(conversationId, muted, caller);
        return ResponseEntity.noContent().build();
    }
}
