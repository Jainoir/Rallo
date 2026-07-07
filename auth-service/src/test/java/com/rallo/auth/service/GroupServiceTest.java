package com.rallo.auth.service;

import com.rallo.auth.dto.GroupMemberResponse;
import com.rallo.auth.dto.GroupResponse;
import com.rallo.auth.model.Group;
import com.rallo.auth.model.GroupMember;
import com.rallo.auth.model.User;
import com.rallo.auth.repository.GroupMemberRepository;
import com.rallo.auth.repository.GroupRepository;
import com.rallo.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class GroupServiceTest {

    private static final String OWNER = "user-owner";

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GroupService groupService;

    private Group group(String id, String ownerId) {
        Group g = new Group();
        g.setId(id);
        g.setName("Gym crew");
        g.setOwnerId(ownerId);
        return g;
    }

    private User user(String id, String username) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        return u;
    }

    @Test
    void createMakesCreatorOwnerAndFirstMember() {
        when(groupRepository.save(any(Group.class))).thenAnswer(inv -> {
            Group g = inv.getArgument(0);
            g.setId("group-1");
            return g;
        });
        when(groupMemberRepository.save(any(GroupMember.class))).thenAnswer(inv -> inv.getArgument(0));

        GroupResponse response = groupService.create(OWNER, "Gym crew");

        assertThat(response.ownerId()).isEqualTo(OWNER);
        ArgumentCaptor<GroupMember> member = ArgumentCaptor.forClass(GroupMember.class);
        verify(groupMemberRepository).save(member.capture());
        assertThat(member.getValue().getUserId()).isEqualTo(OWNER);
        assertThat(member.getValue().getGroupId()).isEqualTo("group-1");
    }

    @Test
    void addMemberRejectsNonOwner() {
        when(groupRepository.findById("group-1")).thenReturn(Optional.of(group("group-1", OWNER)));

        assertThatThrownBy(() -> groupService.addMember("user-imposter", "group-1", "jane"))
                .isInstanceOf(NoSuchElementException.class);
        verify(groupMemberRepository, never()).save(any());
    }

    @Test
    void addMemberRejectsDuplicates() {
        when(groupRepository.findById("group-1")).thenReturn(Optional.of(group("group-1", OWNER)));
        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(user("user-jane", "jane")));
        when(groupMemberRepository.existsByGroupIdAndUserId("group-1", "user-jane")).thenReturn(true);

        assertThatThrownBy(() -> groupService.addMember(OWNER, "group-1", "jane"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already a member");
    }

    @Test
    void addMemberAddsUserByUsername() {
        when(groupRepository.findById("group-1")).thenReturn(Optional.of(group("group-1", OWNER)));
        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(user("user-jane", "jane")));
        when(groupMemberRepository.existsByGroupIdAndUserId("group-1", "user-jane")).thenReturn(false);
        when(groupMemberRepository.save(any(GroupMember.class))).thenAnswer(inv -> inv.getArgument(0));

        GroupMemberResponse response = groupService.addMember(OWNER, "group-1", "jane");

        assertThat(response).isEqualTo(new GroupMemberResponse("user-jane", "jane"));
    }

    @Test
    void membersHiddenFromNonMembers() {
        when(groupMemberRepository.existsByGroupIdAndUserId("group-1", "user-outsider")).thenReturn(false);

        assertThatThrownBy(() -> groupService.members("user-outsider", "group-1"))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void membersListedWithUsernamesForMembers() {
        when(groupMemberRepository.existsByGroupIdAndUserId("group-1", OWNER)).thenReturn(true);
        GroupMember m1 = new GroupMember();
        m1.setGroupId("group-1");
        m1.setUserId(OWNER);
        GroupMember m2 = new GroupMember();
        m2.setGroupId("group-1");
        m2.setUserId("user-jane");
        when(groupMemberRepository.findByGroupId("group-1")).thenReturn(List.of(m1, m2));
        when(userRepository.findAllById(List.of(OWNER, "user-jane")))
                .thenReturn(List.of(user(OWNER, "sanjai"), user("user-jane", "jane")));

        List<GroupMemberResponse> members = groupService.members(OWNER, "group-1");

        assertThat(members).containsExactly(
                new GroupMemberResponse(OWNER, "sanjai"),
                new GroupMemberResponse("user-jane", "jane"));
    }
}
