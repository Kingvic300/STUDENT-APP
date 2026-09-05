package chat.halixe.chat.service;

import chat.halixe.chat.dtos.request.AddConversationMemberRequest;
import chat.halixe.chat.dtos.request.CreateConversationRequest;
import chat.halixe.chat.dtos.response.ConversationMemberResponse;
import chat.halixe.chat.dtos.response.ConversationResponse;
import chat.halixe.chat.security.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ConversationService {

    ConversationResponse createConversation(CreateConversationRequest request, AuthenticatedUser caller);

    ConversationResponse getConversation(UUID conversationId, AuthenticatedUser caller);

    Page<ConversationResponse> listConversations(Pageable pageable, AuthenticatedUser caller);

    ConversationMemberResponse addMember(UUID conversationId, AddConversationMemberRequest request, AuthenticatedUser caller);

    void removeMember(UUID conversationId, UUID userId, AuthenticatedUser caller);

    void leaveConversation(UUID conversationId, AuthenticatedUser caller);

    List<ConversationMemberResponse> listMembers(UUID conversationId, AuthenticatedUser caller);

    ConversationMemberResponse promoteMember(UUID conversationId, UUID userId, AuthenticatedUser caller);

    ConversationMemberResponse demoteMember(UUID conversationId, UUID userId, AuthenticatedUser caller);

    void muteConversation(UUID conversationId, boolean muted, AuthenticatedUser caller);
}
