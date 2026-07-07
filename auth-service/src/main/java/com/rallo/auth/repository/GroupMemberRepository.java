package com.rallo.auth.repository;

import com.rallo.auth.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, String> {
    List<GroupMember> findByUserId(String userId);
    List<GroupMember> findByGroupId(String groupId);
    boolean existsByGroupIdAndUserId(String groupId, String userId);
}
