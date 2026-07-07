package com.rallo.auth.service;

import com.rallo.auth.dto.FriendRequestResponse;
import com.rallo.auth.dto.FriendResponse;
import com.rallo.auth.model.Friendship;
import com.rallo.auth.model.FriendshipStatus;
import com.rallo.auth.model.User;
import com.rallo.auth.repository.FriendshipRepository;
import com.rallo.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    private static final String ME = "user-me";
    private static final String OTHER = "user-other";

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FriendService friendService;

    private User user(String id, String username) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        return u;
    }

    @Test
    void sendRequestCreatesPendingFriendship() {
        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(user(OTHER, "jane")));
        when(friendshipRepository.findBetween(ME, OTHER)).thenReturn(Optional.empty());
        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(ME)).thenReturn(Optional.of(user(ME, "me")));

        FriendRequestResponse response = friendService.sendRequest(ME, "jane");

        assertThat(response.requesterId()).isEqualTo(ME);
        assertThat(response.requesterUsername()).isEqualTo("me");
    }

    @Test
    void sendRequestRejectsUnknownUsername() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.sendRequest(ME, "ghost"))
                .isInstanceOf(NoSuchElementException.class);
        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void sendRequestRejectsSelf() {
        when(userRepository.findByUsername("me")).thenReturn(Optional.of(user(ME, "me")));

        assertThatThrownBy(() -> friendService.sendRequest(ME, "me"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("yourself");
    }

    @Test
    void sendRequestRejectsDuplicateInEitherDirection() {
        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(user(OTHER, "jane")));
        when(friendshipRepository.findBetween(ME, OTHER)).thenReturn(Optional.of(new Friendship()));

        assertThatThrownBy(() -> friendService.sendRequest(ME, "jane"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void acceptMarksRequestAccepted() {
        Friendship pending = new Friendship();
        pending.setRequesterId(OTHER);
        pending.setAddresseeId(ME);
        when(friendshipRepository.findByIdAndAddresseeIdAndStatus("req-1", ME, FriendshipStatus.PENDING))
                .thenReturn(Optional.of(pending));

        friendService.accept(ME, "req-1");

        assertThat(pending.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
    }

    @Test
    void acceptRejectsWhenNotAddressee() {
        when(friendshipRepository.findByIdAndAddresseeIdAndStatus("req-1", ME, FriendshipStatus.PENDING))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.accept(ME, "req-1"))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void listFriendsReturnsTheOtherPartyOfEachAcceptedFriendship() {
        Friendship iRequested = new Friendship();
        iRequested.setRequesterId(ME);
        iRequested.setAddresseeId(OTHER);
        iRequested.setStatus(FriendshipStatus.ACCEPTED);
        Friendship theyRequested = new Friendship();
        theyRequested.setRequesterId("user-third");
        theyRequested.setAddresseeId(ME);
        theyRequested.setStatus(FriendshipStatus.ACCEPTED);
        when(friendshipRepository.findAcceptedFor(ME)).thenReturn(List.of(iRequested, theyRequested));
        when(userRepository.findAllById(List.of(OTHER, "user-third")))
                .thenReturn(List.of(user(OTHER, "jane"), user("user-third", "bob")));

        List<FriendResponse> friends = friendService.listFriends(ME);

        assertThat(friends).containsExactly(
                new FriendResponse(OTHER, "jane"),
                new FriendResponse("user-third", "bob"));
    }
}
