package com.rallo.checkin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "checkins", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"goal_id", "checkin_date"})
})
@Getter
@Setter
@NoArgsConstructor
public class Checkin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private LocalDate checkinDate;

    @Column(nullable = false, updatable = false)
    private Instant checkedInAt = Instant.now();

    @Column(length = 500)
    private String notes;
}
