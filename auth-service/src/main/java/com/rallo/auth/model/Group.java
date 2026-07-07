package com.rallo.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "friend_groups")   // "groups" is a reserved word in SQL
@Getter
@Setter
@NoArgsConstructor
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private String ownerId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
