package chat.halixe.chat.service;

import chat.halixe.chat.data.enums.MessageType;
import chat.halixe.chat.data.model.Conversation;
import chat.halixe.chat.data.model.Message;
import chat.halixe.chat.data.model.MessageAttachment;
import chat.halixe.chat.data.repository.ConversationMemberRepository;
import chat.halixe.chat.data.repository.ConversationRepository;
import chat.halixe.chat.data.repository.MessageAttachmentRepository;
import chat.halixe.chat.data.repository.MessageRepository;
import chat.halixe.chat.dtos.request.AttachmentRequest;
import chat.halixe.chat.dtos.request.DeleteMessageRequest;
import chat.halixe.chat.dtos.request.EditMessageRequest;
import chat.halixe.chat.dtos.request.SendMessageRequest;
import chat.halixe.chat.dtos.response.DeleteMessageResponse;
import chat.halixe.chat.dtos.response.MessageResponse;
import chat.halixe.chat.exception.ConversationNotFoundException;
import chat.halixe.chat.exception.ForbiddenException;
import chat.halixe.chat.websocket.WebSocketEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock private MessageRepository messageRepository;
    @Mock private ConversationRepository conversationRepository;
    @Mock private ConversationMemberRepository memberRepository;
    @Mock private MessageAttachmentRepository attachmentRepository;
    @Mock private WebSocketEventPublisher eventPublisher;
    @Mock private NotificationService notificationService;

    @InjectMocks private MessageServiceImpl service;

    private static final UUID CONV_ID = UUID.randomUUID();
    private static final UUID SENDER_ID = UUID.randomUUID();
    private static final UUID MSG_ID = UUID.randomUUID();

    // ── sendMessage ────────────────────────────────────────────────────────

    @Test
    void sendMessageSavesAndBroadcasts() {
        SendMessageRequest request = buildSendRequest();
        when(conversationRepository.findById(CONV_ID)).thenReturn(Optional.of(new Conversation()));
        when(memberRepository.existsByConversationIdAndUserIdAndLeftAtIsNull(CONV_ID, SENDER_ID)).thenReturn(true);
        when(messageRepository.save(any())).thenAnswer(inv -> {
            Message m = inv.getArgument(0);
            m.setId(MSG_ID);
            return m;
        });

        MessageResponse response = service.sendMessage(request);

        assertThat(response.getConversationId()).isEqualTo(CONV_ID);
        assertThat(response.getSenderId()).isEqualTo(SENDER_ID);
        verify(eventPublisher).publishMessageSent(any());
        verify(notificationService).processMentions(any());
    }

    @Test
    void sendMessageSavesAttachments() {
        AttachmentRequest att = new AttachmentRequest();
        att.setFileId(UUID.randomUUID());
        att.setFileName("doc.pdf");
        att.setMimeType("application/pdf");
        att.setSize(2048L);
        att.setUrl("https://cdn.example.com/doc.pdf");

        SendMessageRequest request = buildSendRequest();
        request.setAttachments(List.of(att));

        when(conversationRepository.findById(CONV_ID)).thenReturn(Optional.of(new Conversation()));
        when(memberRepository.existsByConversationIdAndUserIdAndLeftAtIsNull(CONV_ID, SENDER_ID)).thenReturn(true);
        when(messageRepository.save(any())).thenAnswer(inv -> {
            Message m = inv.getArgument(0);
            m.setId(MSG_ID);
            return m;
        });

        MessageAttachment savedAtt = MessageAttachment.builder()
                .id(UUID.randomUUID()).messageId(MSG_ID).fileName("doc.pdf").size(2048L).build();
        when(attachmentRepository.saveAll(any())).thenReturn(List.of(savedAtt));

        MessageResponse response = service.sendMessage(request);

        assertThat(response.getAttachments()).hasSize(1);
        assertThat(response.getAttachments().get(0).getFileName()).isEqualTo("doc.pdf");

        ArgumentCaptor<List<MessageAttachment>> captor = ArgumentCaptor.forClass(List.class);
        verify(attachmentRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getFileName()).isEqualTo("doc.pdf");
    }

    @Test
    void sendMessageThrowsWhenConversationNotFound() {
        when(conversationRepository.findById(CONV_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.sendMessage(buildSendRequest()))
                .isInstanceOf(ConversationNotFoundException.class);

        verify(messageRepository, never()).save(any());
    }

    @Test
    void sendMessageThrowsWhenCallerNotMember() {
        when(conversationRepository.findById(CONV_ID)).thenReturn(Optional.of(new Conversation()));
        when(memberRepository.existsByConversationIdAndUserIdAndLeftAtIsNull(CONV_ID, SENDER_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.sendMessage(buildSendRequest()))
                .isInstanceOf(ForbiddenException.class);
    }

    // ── editMessage ────────────────────────────────────────────────────────

    @Test
    void editMessageUpdatesContentAndBroadcasts() {
        Message message = buildMessage();
        when(messageRepository.findById(MSG_ID)).thenReturn(Optional.of(message));
        when(messageRepository.save(any())).thenReturn(message);

        EditMessageRequest request = EditMessageRequest.builder()
                .messageId(MSG_ID)
                .requesterId(SENDER_ID)
                .content("updated content")
                .build();

        MessageResponse response = service.editMessage(request);

        assertThat(response.getContent()).isEqualTo("updated content");
        assertThat(message.getEditedAt()).isNotNull();
        verify(eventPublisher).publishMessageEdited(any());
    }

    @Test
    void editMessageThrowsWhenNotOwner() {
        Message message = buildMessage();
        when(messageRepository.findById(MSG_ID)).thenReturn(Optional.of(message));

        EditMessageRequest request = EditMessageRequest.builder()
                .messageId(MSG_ID)
                .requesterId(UUID.randomUUID()) // different user
                .content("updated")
                .build();

        assertThatThrownBy(() -> service.editMessage(request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("own messages");
    }

    @Test
    void editMessageThrowsWhenMessageNotFound() {
        when(messageRepository.findById(MSG_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editMessage(
                EditMessageRequest.builder().messageId(MSG_ID).requesterId(SENDER_ID).content("x").build()))
                .isInstanceOf(ConversationNotFoundException.class);
    }

    // ── deleteMessage ──────────────────────────────────────────────────────

    @Test
    void deleteMessageSetsDeletedAtAndBroadcasts() {
        Message message = buildMessage();
        when(messageRepository.findById(MSG_ID)).thenReturn(Optional.of(message));
        when(messageRepository.save(any())).thenReturn(message);

        DeleteMessageRequest request = DeleteMessageRequest.builder()
                .messageId(MSG_ID)
                .requesterId(SENDER_ID)
                .build();

        DeleteMessageResponse response = service.deleteMessage(request);

        assertThat(response.getMessage()).isEqualTo("deleted");
        assertThat(message.getDeletedAt()).isNotNull();
        verify(eventPublisher).publishMessageDeleted(any());
    }

    @Test
    void deleteMessageThrowsWhenNotOwner() {
        Message message = buildMessage();
        when(messageRepository.findById(MSG_ID)).thenReturn(Optional.of(message));

        assertThatThrownBy(() -> service.deleteMessage(
                DeleteMessageRequest.builder().messageId(MSG_ID).requesterId(UUID.randomUUID()).build()))
                .isInstanceOf(ForbiddenException.class);
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private SendMessageRequest buildSendRequest() {
        return SendMessageRequest.builder()
                .conversationId(CONV_ID)
                .senderId(SENDER_ID)
                .content("hello")
                .messageType(MessageType.TEXT)
                .build();
    }

    private Message buildMessage() {
        return Message.builder()
                .id(MSG_ID)
                .conversationId(CONV_ID)
                .senderId(SENDER_ID)
                .content("original content")
                .type(MessageType.TEXT)
                .build();
    }
}
