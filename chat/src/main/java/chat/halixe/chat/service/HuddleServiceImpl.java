package chat.halixe.chat.service;

import chat.halixe.chat.client.CallServiceClient;
import chat.halixe.chat.data.repository.ConversationMemberRepository;
import chat.halixe.chat.data.repository.ConversationRepository;
import chat.halixe.chat.dtos.response.HuddleResponse;
import chat.halixe.chat.event.ChatEvent;
import chat.halixe.chat.exception.ConversationNotFoundException;
import chat.halixe.chat.exception.ForbiddenException;
import chat.halixe.chat.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HuddleServiceImpl implements HuddleService {

    private final CallServiceClient callServiceClient;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public HuddleResponse initiateHuddle(UUID conversationId, AuthenticatedUser caller) {
        if (!conversationRepository.existsByIdAndOrganisationId(conversationId, caller.organisationId())) {
            throw new ConversationNotFoundException("Conversation not found: " + conversationId);
        }

        if (!memberRepository.existsByConversationIdAndUserIdAndLeftAtIsNull(conversationId, caller.userId())) {
            throw new ForbiddenException("You are not a member of this conversation");
        }

        HuddleResponse session = callServiceClient.createCallSession(conversationId, caller.userId());

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + conversationId,
                new ChatEvent("CALL_INITIATED", session)
        );

        return session;
    }

    @Override
    public HuddleResponse joinHuddle(String callId, AuthenticatedUser caller) {
        HuddleResponse session = callServiceClient.joinCallSession(callId, caller.userId());

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + session.getConversationId(),
                new ChatEvent("CALL_JOINED", session)
        );

        return session;
    }

    @Override
    public void declineHuddle(String callId, AuthenticatedUser caller) {
        callServiceClient.declineCallSession(callId, caller.userId());

        messagingTemplate.convertAndSendToUser(
                caller.userId().toString(),
                "/queue/calls",
                new ChatEvent("CALL_DECLINED", callId)
        );
    }

    @Override
    public void endHuddle(String callId, AuthenticatedUser caller) {
        callServiceClient.endCallSession(callId, caller.userId());
    }
}
