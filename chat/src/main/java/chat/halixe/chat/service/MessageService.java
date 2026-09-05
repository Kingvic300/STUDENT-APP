package chat.halixe.chat.service;

import chat.halixe.chat.dtos.request.*;
import chat.halixe.chat.dtos.response.DeleteMessageResponse;
import chat.halixe.chat.dtos.response.MessageListResponse;
import chat.halixe.chat.dtos.response.MessageResponse;

import java.util.UUID;

public interface MessageService {

    MessageResponse sendMessage(SendMessageRequest request);

    MessageResponse editMessage(EditMessageRequest request);

    DeleteMessageResponse deleteMessage(DeleteMessageRequest request);

    MessageResponse getMessage(GetMessageRequest request);

    MessageListResponse listMessage(ListMessageRequest request);

    MessageListResponse listThreadReplies(ListThreadRepliesRequest request);

    MessageListResponse listRepliesTo(UUID messageId);

    MessageListResponse getRecentMessages(UUID conversationId);
}