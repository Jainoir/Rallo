package com.rallo.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "group_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"group_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "group_id", nullable = false)
    private String groupId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false, updatable = false)
    private Instant joinedAt = Instant.now();
}
