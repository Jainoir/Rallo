package com.rallo.checkin.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Synchronous client for the auth service, which owns the social graph.
 * Requests are stamped with the gateway shared secret (the same trust
 * mechanism the gateway uses) and carry the caller's identity so the auth
 * service can enforce ownership/membership rules.
 */
@Component
@Slf4j
public class AuthClient {

    public record FriendDto(String userId, String username) {}
    public record MemberDto(String userId, String username) {}

    private final RestClient restClient;

    public AuthClient(@Value("${rallo.auth-service.url}") String authServiceUrl,
                      @Value("${gateway.shared-secret:}") String gatewaySecret) {
        this.restClient = RestClient.builder()
                .baseUrl(authServiceUrl)
                .defaultHeaders(headers -> {
                    if (gatewaySecret != null && !gatewaySecret.isBlank()) {
                        headers.set("X-Gateway-Secret", gatewaySecret);
                    }
                })
                .build();
    }

    public List<FriendDto> listFriends(String userId) {
        return restClient.get()
                .uri("/api/friends")
                .header("X-User-Id", userId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<MemberDto> groupMembers(String groupId, String userId) {
        return restClient.get()
                .uri("/api/groups/{groupId}/members", groupId)
                .header("X-User-Id", userId)
                .retrieve()
                .onStatus(status -> status.value() == 404, (request, response) -> {
                    // auth's membership check failed — surface as our own 404
                    throw new NoSuchElementException("Group not found: " + groupId);
                })
                .body(new ParameterizedTypeReference<>() {});
    }
}
