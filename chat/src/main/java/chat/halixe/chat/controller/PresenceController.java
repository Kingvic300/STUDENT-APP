package chat.halixe.chat.controller;

import chat.halixe.chat.dtos.request.UpdatePresenceRequest;
import chat.halixe.chat.dtos.response.PresenceResponse;
import chat.halixe.chat.security.AuthenticatedUser;
import chat.halixe.chat.service.PresenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;

    @MessageMapping("/presence.update")
    public void updatePresenceWs(
            @Payload UpdatePresenceRequest request,
            Principal principal
    ) {
        if (!(principal instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth)) {
            return;
        }
        AuthenticatedUser caller = (AuthenticatedUser) auth.getPrincipal();
        presenceService.updatePresence(request.getStatus(), caller);
    }

    @RestController
    @RequestMapping("/api/v1/presence")
    @RequiredArgsConstructor
    static class PresenceRestController {

        private final PresenceService presenceService;

        @PutMapping
        public ResponseEntity<Void> update(
                @Valid @RequestBody UpdatePresenceRequest request,
                @AuthenticationPrincipal AuthenticatedUser caller
        ) {
            presenceService.updatePresence(request.getStatus(), caller);
            return ResponseEntity.noContent().build();
        }

        @GetMapping("/{userId}")
        public ResponseEntity<PresenceResponse> get(@PathVariable UUID userId) {
            return ResponseEntity.ok(presenceService.getPresence(userId));
        }
    }
}
