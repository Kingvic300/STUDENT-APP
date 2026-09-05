package chat.halixe.chat.service;

import chat.halixe.chat.data.model.MessageRead;
import chat.halixe.chat.data.repository.ConversationMemberRepository;
import chat.halixe.chat.data.repository.MessageReadRepository;
import chat.halixe.chat.data.repository.MessageRepository;
import chat.halixe.chat.exception.ConversationNotFoundException;
import chat.halixe.chat.exception.ForbiddenException;
import chat.halixe.chat.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageReadServiceImpl implements MessageReadService {

    private final MessageReadRepository messageReadRepository;
    private final MessageRepository messageRepository;
    private final ConversationMemberRepository memberRepository;

    @Override
    @Transactional
    public void markAsRead(UUID conversationId, UUID messageId, AuthenticatedUser caller) {
        messageRepository.findByIdAndConversationId(messageId, conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Message not found: " + messageId));

        if (!memberRepository.existsByConversationIdAndUserIdAndLeftAtIsNull(conversationId, caller.userId())) {
            throw new ForbiddenException("You are not a member of this conversation");
        }

        if (!messageReadRepository.existsByMessageIdAndUserId(messageId, caller.userId())) {
            messageReadRepository.save(MessageRead.builder()
                    .messageId(messageId)
                    .userId(caller.userId())
                    .readAt(Instant.now())
                    .build());
        }

        memberRepository.updateLastReadMessage(conversationId, caller.userId(), messageId);
    }

    @Override
    public long countUnread(UUID conversationId, AuthenticatedUser caller) {
        return memberRepository.findByConversationIdAndUserId(conversationId, caller.userId())
                .map(member -> {
                    if (member.getLastReadMessageId() == null) {
                        return messageRepository.findByConversationIdOrderByCreatedAtDesc(
                                conversationId,
                                org.springframework.data.domain.PageRequest.of(0, 1)
                        ).getTotalElements();
                    }
                    return 0L;
                })
                .orElse(0L);
    }
}
