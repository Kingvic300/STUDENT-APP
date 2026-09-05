package chat.halixe.chat.service;

import chat.halixe.chat.data.model.Message;
import chat.halixe.chat.data.model.MessageAttachment;
import chat.halixe.chat.data.repository.ConversationMemberRepository;
import chat.halixe.chat.data.repository.ConversationRepository;
import chat.halixe.chat.data.repository.MessageAttachmentRepository;
import chat.halixe.chat.data.repository.MessageRepository;
import chat.halixe.chat.service.NotificationService;
import chat.halixe.chat.dtos.request.*;
import chat.halixe.chat.dtos.response.DeleteMessageResponse;
import chat.halixe.chat.dtos.response.MessageListResponse;
import chat.halixe.chat.dtos.response.MessageResponse;
import chat.halixe.chat.exception.ConversationNotFoundException;
import chat.halixe.chat.exception.ForbiddenException;
import chat.halixe.chat.mappers.MessageMapper;
import chat.halixe.chat.websocket.WebSocketEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final MessageAttachmentRepository attachmentRepository;
    private final WebSocketEventPublisher eventPublisher;
    private final NotificationService notificationService;

    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 100;

    @Override
    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request) {
        conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new ConversationNotFoundException(
                        "Conversation not found: " + request.getConversationId()));

        if (!memberRepository.existsByConversationIdAndUserIdAndLeftAtIsNull(
                request.getConversationId(), request.getSenderId())) {
            throw new ForbiddenException("You are not a member of this conversation");
        }

        Message saved = messageRepository.save(MessageMapper.mapToSendMessage(request));

        List<MessageAttachment> attachments = saveAttachments(saved.getId(), request.getAttachments());

        notificationService.processMentions(saved);

        MessageResponse response = MessageMapper.mapToSendMessageResponse(saved, attachments);
        eventPublisher.publishMessageSent(response);
        return response;
    }

    @Override
    @Transactional
    public MessageResponse editMessage(EditMessageRequest request) {
        Message message = messageRepository.findById(request.getMessageId())
                .orElseThrow(() -> new ConversationNotFoundException(
                        "Message not found: " + request.getMessageId()));

        if (!message.getSenderId().equals(request.getRequesterId())) {
            throw new ForbiddenException("You can only edit your own messages");
        }

        MessageMapper.mapToEditMessage(request, message);
        message.setEditedAt(Instant.now());
        messageRepository.save(message);

        MessageResponse response = MessageMapper.mapToEditMessageResponse(message);
        eventPublisher.publishMessageEdited(response);
        return response;
    }

    @Override
    @Transactional
    public DeleteMessageResponse deleteMessage(DeleteMessageRequest request) {
        Message message = messageRepository.findById(request.getMessageId())
                .orElseThrow(() -> new ConversationNotFoundException(
                        "Message not found: " + request.getMessageId()));

        if (!message.getSenderId().equals(request.getRequesterId())) {
            throw new ForbiddenException("You can only delete your own messages");
        }

        message.setDeletedAt(Instant.now());
        messageRepository.save(message);

        eventPublisher.publishMessageDeleted(MessageMapper.mapToSendMessageResponse(message));
        return MessageMapper.mapToDeleteMessageResponse("deleted");
    }

    @Override
    public MessageResponse getMessage(GetMessageRequest request) {
        Message message = messageRepository
                .findByIdAndConversationId(request.getMessageId(), request.getConversationId())
                .orElseThrow(() -> new ConversationNotFoundException(
                        "Message not found: " + request.getMessageId()));

        List<MessageAttachment> attachments = attachmentRepository.findByMessageId(message.getId());
        return MessageMapper.mapToGetMessageResponse(message, attachments);
    }

    @Override
    public MessageListResponse listMessage(ListMessageRequest request) {
        conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new ConversationNotFoundException(
                        "Conversation not found: " + request.getConversationId()));

        Pageable pageable = normalizePageable(request.getPageable(), request.isNewestFirst());
        Page<Message> messagePage = request.isNewestFirst()
                ? messageRepository.findByConversationIdOrderByCreatedAtDesc(request.getConversationId(), pageable)
                : messageRepository.findByConversationIdOrderByCreatedAtAsc(request.getConversationId(), pageable);

        return enrichedListResponse(messagePage);
    }

    @Override
    public MessageListResponse listThreadReplies(ListThreadRepliesRequest request) {
        Pageable pageable = normalizePageable(request.getPageable(), false);
        Page<Message> messagePage = messageRepository.findByThreadIdOrderByCreatedAtAsc(
                request.getThreadId(), pageable);
        return enrichedListResponse(messagePage);
    }

    @Override
    public MessageListResponse listRepliesTo(UUID messageId) {
        List<Message> replies = messageRepository.findByReplyToMessageIdOrderByCreatedAtAsc(messageId);
        List<UUID> ids = replies.stream().map(Message::getId).toList();
        List<MessageAttachment> allAttachments = attachmentRepository.findByMessageIdIn(ids);

        List<MessageResponse> responses = replies.stream()
                .map(m -> MessageMapper.mapToSendMessageResponse(m,
                        allAttachments.stream()
                                .filter(a -> a.getMessageId().equals(m.getId()))
                                .toList()))
                .toList();

        return MessageListResponse.builder()
                .messages(responses)
                .page(0)
                .size(responses.size())
                .totalElements(responses.size())
                .totalPages(responses.isEmpty() ? 0 : 1)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }

    @Override
    public MessageListResponse getRecentMessages(UUID conversationId) {
        conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(
                        "Conversation not found: " + conversationId));

        Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Message> messagePage = messageRepository.findByConversationIdOrderByCreatedAtDesc(
                conversationId, pageable);
        return enrichedListResponse(messagePage);
    }

    private List<MessageAttachment> saveAttachments(UUID messageId, List<AttachmentRequest> requests) {
        if (requests == null || requests.isEmpty()) return List.of();

        List<MessageAttachment> entities = requests.stream()
                .map(r -> MessageAttachment.builder()
                        .messageId(messageId)
                        .fileId(r.getFileId())
                        .fileName(r.getFileName())
                        .mimeType(r.getMimeType())
                        .size(r.getSize())
                        .url(r.getUrl())
                        .build())
                .toList();

        return attachmentRepository.saveAll(entities);
    }

    private MessageListResponse enrichedListResponse(Page<Message> messagePage) {
        List<UUID> ids = messagePage.getContent().stream().map(Message::getId).toList();
        List<MessageAttachment> allAttachments = ids.isEmpty()
                ? List.of()
                : attachmentRepository.findByMessageIdIn(ids);

        List<MessageResponse> responses = messagePage.getContent().stream()
                .map(m -> MessageMapper.mapToSendMessageResponse(m,
                        allAttachments.stream()
                                .filter(a -> a.getMessageId().equals(m.getId()))
                                .toList()))
                .toList();

        return MessageListResponse.builder()
                .messages(responses)
                .page(messagePage.getNumber())
                .size(messagePage.getSize())
                .totalElements(messagePage.getTotalElements())
                .totalPages(messagePage.getTotalPages())
                .hasNext(messagePage.hasNext())
                .hasPrevious(messagePage.hasPrevious())
                .build();
    }

    private Pageable normalizePageable(Pageable pageable, boolean newestFirst) {
        int page = 0;
        int size = DEFAULT_PAGE_SIZE;

        if (pageable != null) {
            page = Math.max(pageable.getPageNumber(), 0);
            size = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);
        }

        Sort sort = newestFirst
                ? Sort.by(Sort.Direction.DESC, "createdAt")
                : Sort.by(Sort.Direction.ASC, "createdAt");

        return PageRequest.of(page, size, sort);
    }
}
