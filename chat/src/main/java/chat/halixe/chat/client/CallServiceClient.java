package chat.halixe.chat.client;

import chat.halixe.chat.dtos.response.HuddleResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class CallServiceClient {

    private final RestClient restClient;

    public CallServiceClient(@Value("${call-service.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public HuddleResponse createCallSession(UUID conversationId, UUID initiatorId) {
        return restClient.post()
                .uri("/api/v1/calls")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "conversationId", conversationId.toString(),
                        "initiatorId", initiatorId.toString()
                ))
                .retrieve()
                .body(HuddleResponse.class);
    }

    public HuddleResponse joinCallSession(String callId, UUID userId) {
        return restClient.post()
                .uri("/api/v1/calls/{callId}/join", callId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("userId", userId.toString()))
                .retrieve()
                .body(HuddleResponse.class);
    }

    public void endCallSession(String callId, UUID userId) {
        restClient.delete()
                .uri("/api/v1/calls/{callId}?userId={userId}", callId, userId)
                .retrieve()
                .toBodilessEntity();
    }

    public void declineCallSession(String callId, UUID userId) {
        restClient.post()
                .uri("/api/v1/calls/{callId}/decline", callId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("userId", userId.toString()))
                .retrieve()
                .toBodilessEntity();
    }
}
