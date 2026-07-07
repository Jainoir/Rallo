package com.rallo.auth.service;

import com.rallo.auth.dto.FriendRequestResponse;
import com.rallo.auth.dto.FriendResponse;
import com.rallo.auth.model.Friendship;
import com.rallo.auth.model.FriendshipStatus;
import com.rallo.auth.model.User;
import com.rallo.auth.repository.FriendshipRepository;
import com.rallo.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    @Transactional
    public FriendRequestResponse sendRequest(String userId, String targetUsername) {
        User target = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new NoSuchElementException("No user named " + targetUsername));

        if (target.getId().equals(userId)) {
            throw new IllegalArgumentException("You cannot add yourself as a friend");
        }
        if (friendshipRepository.findBetween(userId, target.getId()).isPresent()) {
            throw new IllegalArgumentException("A friend request already exists between you two");
        }

        Friendship friendship = new Friendship();
        friendship.setRequesterId(userId);
        friendship.setAddresseeId(target.getId());
        friendshipRepository.save(friendship);

        String requesterUsername = userRepository.findById(userId)
                .map(User::getUsername).orElse("unknown");
        return new FriendRequestResponse(
                friendship.getId(), userId, requesterUsername, friendship.getCreatedAt());
    }

    /** Only the addressee of a pending request may accept it. */
    @Transactional
    public void accept(String userId, String requestId) {
        Friendship friendship = friendshipRepository
                .findByIdAndAddresseeIdAndStatus(requestId, userId, FriendshipStatus.PENDING)
                .orElseThrow(() -> new NoSuchElementException("Friend request not found: " + requestId));
        friendship.setStatus(FriendshipStatus.ACCEPTED);
    }

    public List<FriendRequestResponse> incomingRequests(String userId) {
        List<Friendship> pending = friendshipRepository
                .findByAddresseeIdAndStatus(userId, FriendshipStatus.PENDING);
        Map<String, String> usernames = usernamesOf(
                pending.stream().map(Friendship::getRequesterId).toList());
        return pending.stream()
                .map(f -> new FriendRequestResponse(
                        f.getId(), f.getRequesterId(),
                        usernames.getOrDefault(f.getRequesterId(), "unknown"), f.getCreatedAt()))
                .toList();
    }

    public List<FriendResponse> listFriends(String userId) {
        List<String> friendIds = friendshipRepository.findAcceptedFor(userId).stream()
                .map(f -> f.getRequesterId().equals(userId) ? f.getAddresseeId() : f.getRequesterId())
                .toList();
        Map<String, String> usernames = usernamesOf(friendIds);
        return friendIds.stream()
                .map(id -> new FriendResponse(id, usernames.getOrDefault(id, "unknown")))
                .toList();
    }

    private Map<String, String> usernamesOf(List<String> userIds) {
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername, (a, b) -> a,
                        () -> new java.util.HashMap<>()));
    }
}
