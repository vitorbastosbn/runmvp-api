package com.runmvp.friendship.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.runmvp.BaseIntegrationTest;
import com.runmvp.auth.adapter.in.web.AuthResponse;
import com.runmvp.auth.application.port.out.GoogleTokenVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FriendshipControllerIT extends BaseIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;
    @MockitoBean private GoogleTokenVerifier googleTokenVerifier;

    private String tokenA;
    private String tokenB;
    private Long userIdA;
    private Long userIdB;
    private String nameA;
    private String nameB;

    @BeforeEach
    void loginTwoUsers() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        nameA = "User A " + unique;
        nameB = "User B " + unique;

        AuthResponse a = login("token-a-" + unique, "sub-a-" + unique,
            "a-" + unique + "@x.com", nameA);
        AuthResponse b = login("token-b-" + unique, "sub-b-" + unique,
            "b-" + unique + "@x.com", nameB);

        tokenA = a.accessToken();
        tokenB = b.accessToken();
        userIdA = a.user().id();
        userIdB = b.user().id();
    }

    private AuthResponse login(String idToken, String subject, String email, String name) {
        when(googleTokenVerifier.verify(idToken)).thenReturn(
            new GoogleTokenVerifier.GoogleIdTokenPayload(subject, email, name, null));
        return restTemplate.postForEntity(
            "/auth/google", Map.of("idToken", idToken), AuthResponse.class).getBody();
    }

    private HttpHeaders bearer(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        return h;
    }

    private Long sendRequestFromAToB() {
        ResponseEntity<JsonNode> resp = restTemplate.exchange(
            "/friend-requests", HttpMethod.POST,
            new HttpEntity<>(Map.of("recipientId", userIdB), bearer(tokenA)),
            JsonNode.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return resp.getBody().get("id").asLong();
    }

    @Test
    void sendRequest_returns201WithId() {
        Long id = sendRequestFromAToB();
        assertThat(id).isPositive();
    }

    @Test
    void sendRequest_toSelf_returns400() {
        ResponseEntity<String> resp = restTemplate.exchange(
            "/friend-requests", HttpMethod.POST,
            new HttpEntity<>(Map.of("recipientId", userIdA), bearer(tokenA)),
            String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).contains("CANNOT_FRIEND_SELF");
    }

    @Test
    void sendRequest_duplicatePending_returns409() {
        sendRequestFromAToB();
        ResponseEntity<String> resp = restTemplate.exchange(
            "/friend-requests", HttpMethod.POST,
            new HttpEntity<>(Map.of("recipientId", userIdB), bearer(tokenA)),
            String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resp.getBody()).contains("FRIEND_REQUEST_ALREADY_PENDING");
    }

    @Test
    void listRequests_recipientSeesPendingRequest() {
        sendRequestFromAToB();

        ResponseEntity<String> resp = restTemplate.exchange(
            "/friend-requests", HttpMethod.GET,
            new HttpEntity<>(bearer(tokenB)), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).contains(nameA);
    }

    @Test
    void accept_byRecipient_returns200_thenBothAreFriends() {
        Long requestId = sendRequestFromAToB();

        ResponseEntity<Void> accept = restTemplate.exchange(
            "/friend-requests/" + requestId + "/accept", HttpMethod.POST,
            new HttpEntity<>(bearer(tokenB)), Void.class);
        assertThat(accept.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> friendsOfA = restTemplate.exchange(
            "/friends", HttpMethod.GET, new HttpEntity<>(bearer(tokenA)), String.class);
        assertThat(friendsOfA.getBody()).contains(nameB);

        ResponseEntity<String> friendsOfB = restTemplate.exchange(
            "/friends", HttpMethod.GET, new HttpEntity<>(bearer(tokenB)), String.class);
        assertThat(friendsOfB.getBody()).contains(nameA);
    }

    @Test
    void accept_byNonRecipient_returns403() {
        Long requestId = sendRequestFromAToB();

        ResponseEntity<String> resp = restTemplate.exchange(
            "/friend-requests/" + requestId + "/accept", HttpMethod.POST,
            new HttpEntity<>(bearer(tokenA)), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(resp.getBody()).contains("NOT_REQUEST_RECIPIENT");
    }

    @Test
    void reject_byRecipient_returns200_requestGone() {
        Long requestId = sendRequestFromAToB();

        ResponseEntity<Void> reject = restTemplate.exchange(
            "/friend-requests/" + requestId + "/reject", HttpMethod.POST,
            new HttpEntity<>(bearer(tokenB)), Void.class);
        assertThat(reject.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> requests = restTemplate.exchange(
            "/friend-requests", HttpMethod.GET,
            new HttpEntity<>(bearer(tokenB)), String.class);
        assertThat(requests.getBody()).doesNotContain(nameA);
    }

    @Test
    void removeFriend_returns204_thenFriendsListEmpty() {
        Long requestId = sendRequestFromAToB();
        restTemplate.exchange(
            "/friend-requests/" + requestId + "/accept", HttpMethod.POST,
            new HttpEntity<>(bearer(tokenB)), Void.class);

        ResponseEntity<Void> del = restTemplate.exchange(
            "/friends/" + userIdB, HttpMethod.DELETE,
            new HttpEntity<>(bearer(tokenA)), Void.class);
        assertThat(del.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> friendsOfA = restTemplate.exchange(
            "/friends", HttpMethod.GET, new HttpEntity<>(bearer(tokenA)), String.class);
        assertThat(friendsOfA.getBody()).doesNotContain(nameB);
    }

    @Test
    void removeFriend_notFriends_returns404() {
        ResponseEntity<String> resp = restTemplate.exchange(
            "/friends/" + userIdB, HttpMethod.DELETE,
            new HttpEntity<>(bearer(tokenA)), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).contains("FRIENDSHIP_NOT_FOUND");
    }
}
