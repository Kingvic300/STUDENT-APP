package chat.halixe.chat.mappers;

import chat.halixe.chat.data.enums.ConversationRole;
import chat.halixe.chat.data.model.Conversation;
import chat.halixe.chat.data.model.ConversationMember;
import chat.halixe.chat.data.model.ConversationSettings;
import chat.halixe.chat.dtos.request.CreateConversationRequest;
import chat.halixe.chat.dtos.response.ConversationMemberResponse;
import chat.halixe.chat.dtos.response.ConversationResponse;
import chat.halixe.chat.security.AuthenticatedUser;

import java.util.UUID;


public class ConversationMapper {

    public static Conversation createConversation(CreateConversationRequest request , AuthenticatedUser user) {
        Conversation conversation  = new Conversation();
        conversation.setType(request.getType());
        conversation.setVisibility(request.getVisibility());
        conversation.setOrganisationId(user.organisationId());
        conversation.setName(request.getName());
        conversation.setCreatedBy(user.userId());

        return conversation;
    }

    public static ConversationSettings createConversationSettings(Conversation conversation) {
        ConversationSettings conversationSettings = new ConversationSettings();
        conversationSettings.setConversationId(conversation.getId());
        return conversationSettings;
    }

    public static ConversationMember createConversationMember(Conversation conversation, AuthenticatedUser user) {
        ConversationMember conversationMember = new ConversationMember();
        conversationMember.setConversationId(conversation.getId());
        conversationMember.setUserId(user.userId());
        conversationMember.setRole(ConversationRole.OWNER);
        return conversationMember;
    }

    public static ConversationMember addConversationMember(Conversation conversation, UUID memberId) {
        return addConversationMember(conversation.getId(), memberId, ConversationRole.MEMBER);
    }

    public static ConversationMember addConversationMember(UUID conversationId, UUID memberId, ConversationRole role) {
        ConversationMember conversationMember = new ConversationMember();
        conversationMember.setConversationId(conversationId);
        conversationMember.setUserId(memberId);
        conversationMember.setRole(role);
        return conversationMember;
    }

    public static ConversationResponse toResponse(Conversation conversation, long memberCount) {
        ConversationResponse conversationResponse = new ConversationResponse();
        conversationResponse.setId(conversation.getId());
        conversationResponse.setType(conversation.getType());
        conversationResponse.setName(conversation.getName());
        conversationResponse.setOrganisationId(conversation.getOrganisationId());
        conversationResponse.setCreatedBy(conversation.getCreatedBy());
        conversationResponse.setCreatedAt(conversation.getCreatedAt());
        conversationResponse.setUpdatedAt(conversation.getUpdatedAt());
        conversationResponse.setMemberCount(memberCount);
        return conversationResponse;
    }

    public static ConversationMemberResponse toMemberResponse(ConversationMember member) {
        ConversationMemberResponse conversationMemberResponse = new ConversationMemberResponse();
        conversationMemberResponse.setId(member.getId());
        conversationMemberResponse.setConversationId(member.getConversationId());
        conversationMemberResponse.setUserId(member.getUserId());
        conversationMemberResponse.setRole(member.getRole());
        conversationMemberResponse.setJoinedAt(member.getJoinedAt());
        conversationMemberResponse.setLeftAt(member.getLeftAt());
        conversationMemberResponse.setMuted(member.isMuted());
        conversationMemberResponse.setLastReadMessageId(member.getLastReadMessageId());


        return conversationMemberResponse;
    }
}
