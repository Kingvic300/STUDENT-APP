package chat.halixe.chat.service;

import chat.halixe.chat.data.model.PinnedMessage;
import chat.halixe.chat.data.repository.ConversationMemberRepository;
import chat.halixe.chat.data.repository.ConversationRepository;
import chat.halixe.chat.data.repository.MessageRepository;
import chat.halixe.chat.data.repository.PinnedMessageRepository;
import chat.halixe.chat.dtos.response.PinnedMessageResponse;
import chat.halixe.chat.exception.ConversationNotFoundException;
import chat.halixe.chat.exception.ForbiddenException;
import chat.halixe.chat.exception.MemberAlreadyExistsException;
import chat.halixe.chat.mappers.MessageMapper;
import chat.halixe.chat.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PinnedMessageServiceImpl implements PinnedMessageService {

    private final PinnedMessageRepository pinnedMessageRepository;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;

    @Override
    @Transactional
    public PinnedMessageResponse pinMessage(UUID conversationId, UUID messageId, AuthenticatedUser caller) {
        requireConversationExists(conversationId, caller.organisationId());
        requireMember(conversationId, caller.userId());

        if (!messageRepository.existsByIdAndConversationId(messageId, conversationId)) {
            throw new ConversationNotFoundException("Message not found in this conversation");
        }

        if (pinnedMessageRepository.existsByConversationIdAndMessageId(conversationId, messageId)) {
            throw new MemberAlreadyExistsException("Message is already pinned");
        }

        PinnedMessage pinned = PinnedMessage.builder()
                .conversationId(conversationId)
                .messageId(messageId)
                .userId(caller.userId())
                .build();

        pinned = pinnedMessageRepository.save(pinned);

        return toPinnedResponse(pinned);
    }

    @Override
    @Transactional
    public void unpinMessage(UUID conversationId, UUID messageId, AuthenticatedUser caller) {
        requireConversationExists(conversationId, caller.organisationId());
        requireMember(conversationId, caller.userId());
        pinnedMessageRepository.deleteByConversationIdAndMessageId(conversationId, messageId);
    }

    @Override
    public List<PinnedMessageResponse> listPinned(UUID conversationId, AuthenticatedUser caller) {
        requireConversationExists(conversationId, caller.organisationId());
        requireMember(conversationId, caller.userId());

        return pinnedMessageRepository
                .findByConversationIdOrderByPinnedAtDesc(conversationId)
                .stream()
                .map(this::toPinnedResponse)
                .toList();
    }

    private void requireConversationExists(UUID conversationId, UUID organisationId) {
        if (!conversationRepository.existsByIdAndOrganisationId(conversationId, organisationId)) {
            throw new ConversationNotFoundException("Conversation not found: " + conversationId);
        }
    }

    private void requireMember(UUID conversationId, UUID userId) {
        if (!memberRepository.existsByConversationIdAndUserIdAndLeftAtIsNull(conversationId, userId)) {
            throw new ForbiddenException("You are not a member of this conversation");
        }
    }

    private PinnedMessageResponse toPinnedResponse(PinnedMessage p) {
        return messageRepository.findById(p.getMessageId())
                .map(msg -> PinnedMessageResponse.builder()
                        .id(p.getId())
                        .conversationId(p.getConversationId())
                        .pinnedBy(p.getUserId())
                        .pinnedAt(p.getPinnedAt())
                        .message(MessageMapper.mapToSendMessageResponse(msg))
                        .build())
                .orElse(PinnedMessageResponse.builder()
                        .id(p.getId())
                        .conversationId(p.getConversationId())
                        .pinnedBy(p.getUserId())
                        .pinnedAt(p.getPinnedAt())
                        .build());
    }
}
