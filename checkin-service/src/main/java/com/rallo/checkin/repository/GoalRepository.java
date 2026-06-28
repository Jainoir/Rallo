package com.rallo.checkin.repository;

import com.rallo.checkin.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, String> {
    List<Goal> findByUserIdAndActiveTrue(String userId);
    Optional<Goal> findByIdAndUserId(String id, String userId);
}
