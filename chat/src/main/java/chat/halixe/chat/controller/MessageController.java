package chat.halixe.chat.controller;

import chat.halixe.chat.dtos.request.*;
import chat.halixe.chat.dtos.response.DeleteMessageResponse;
import chat.halixe.chat.dtos.response.MessageListResponse;
import chat.halixe.chat.dtos.response.MessageResponse;
import chat.halixe.chat.security.AuthenticatedUser;
import chat.halixe.chat.service.MessageReadService;
import chat.halixe.chat.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final MessageReadService messageReadService;

    @PostMapping("/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        request.setSenderId(caller.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(messageService.sendMessage(request));
    }

    @PatchMapping("/messages/{messageId}")
    public ResponseEntity<MessageResponse> editMessage(
            @PathVariable UUID messageId,
            @RequestBody EditMessageRequest request,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        request.setMessageId(messageId);
        request.setRequesterId(caller.userId());
        return ResponseEntity.ok(messageService.editMessage(request));
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<DeleteMessageResponse> deleteMessage(
            @PathVariable UUID messageId,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        DeleteMessageRequest request = new DeleteMessageRequest();
        request.setMessageId(messageId);
        request.setRequesterId(caller.userId());
        return ResponseEntity.ok(messageService.deleteMessage(request));
    }

    @GetMapping("/conversations/{conversationId}/messages/{messageId}")
    public ResponseEntity<MessageResponse> getMessage(
            @PathVariable UUID conversationId,
            @PathVariable UUID messageId
    ) {
        GetMessageRequest request = new GetMessageRequest();
        request.setConversationId(conversationId);
        request.setMessageId(messageId);
        return ResponseEntity.ok(messageService.getMessage(request));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<MessageListResponse> listMessages(
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "false") boolean newestFirst
    ) {
        Pageable pageable = PageRequest.of(page, size);
        ListMessageRequest request = new ListMessageRequest();
        request.setConversationId(conversationId);
        request.setPageable(pageable);
        request.setNewestFirst(newestFirst);
        return ResponseEntity.ok(messageService.listMessage(request));
    }

    @GetMapping("/threads/{threadId}/replies")
    public ResponseEntity<MessageListResponse> listThreadReplies(
            @PathVariable UUID threadId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        ListThreadRepliesRequest request = new ListThreadRepliesRequest();
        request.setThreadId(threadId);
        request.setPageable(pageable);
        return ResponseEntity.ok(messageService.listThreadReplies(request));
    }

    @GetMapping("/messages/{messageId}/replies")
    public ResponseEntity<MessageListResponse> listRepliesTo(@PathVariable UUID messageId) {
        return ResponseEntity.ok(messageService.listRepliesTo(messageId));
    }

    @GetMapping("/conversations/{conversationId}/messages/recent")
    public ResponseEntity<MessageListResponse> getRecentMessages(@PathVariable UUID conversationId) {
        return ResponseEntity.ok(messageService.getRecentMessages(conversationId));
    }

    @PostMapping("/conversations/{conversationId}/messages/{messageId}/read")
    public ResponseEntity<Void> markRead(
            @PathVariable UUID conversationId,
            @PathVariable UUID messageId,
            @AuthenticationPrincipal AuthenticatedUser caller
    ) {
        messageReadService.markAsRead(conversationId, messageId, caller);
        return ResponseEntity.noContent().build();
    }
}
