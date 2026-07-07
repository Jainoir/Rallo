package com.rallo.auth.service;

import com.rallo.auth.dto.GroupMemberResponse;
import com.rallo.auth.dto.GroupResponse;
import com.rallo.auth.model.Group;
import com.rallo.auth.model.GroupMember;
import com.rallo.auth.model.User;
import com.rallo.auth.repository.GroupMemberRepository;
import com.rallo.auth.repository.GroupRepository;
import com.rallo.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public GroupResponse create(String userId, String name) {
        Group group = new Group();
        group.setName(name);
        group.setOwnerId(userId);
        groupRepository.save(group);

        GroupMember owner = new GroupMember();
        owner.setGroupId(group.getId());
        owner.setUserId(userId);
        groupMemberRepository.save(owner);

        return GroupResponse.from(group);
    }

    public List<GroupResponse> myGroups(String userId) {
        List<String> groupIds = groupMemberRepository.findByUserId(userId).stream()
                .map(GroupMember::getGroupId)
                .toList();
        return groupRepository.findAllById(groupIds).stream()
                .map(GroupResponse::from)
                .toList();
    }

    /** Only the group owner may add members. */
    @Transactional
    public GroupMemberResponse addMember(String ownerId, String groupId, String username) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("Group not found: " + groupId));
        if (!group.getOwnerId().equals(ownerId)) {
            throw new NoSuchElementException("Group not found: " + groupId);
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("No user named " + username));
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, user.getId())) {
            throw new IllegalArgumentException(username + " is already a member of this group");
        }

        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(user.getId());
        groupMemberRepository.save(member);

        return new GroupMemberResponse(user.getId(), user.getUsername());
    }

    /** Members are only visible to other members of the same group. */
    public List<GroupMemberResponse> members(String requesterId, String groupId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, requesterId)) {
            throw new NoSuchElementException("Group not found: " + groupId);
        }

        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        Map<String, String> usernames = userRepository
                .findAllById(members.stream().map(GroupMember::getUserId).toList()).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
        return members.stream()
                .map(m -> new GroupMemberResponse(
                        m.getUserId(), usernames.getOrDefault(m.getUserId(), "unknown")))
                .toList();
    }
}
