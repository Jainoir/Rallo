package com.rallo.checkin.repository;

import com.rallo.checkin.model.Checkin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CheckinRepository extends JpaRepository<Checkin, String> {
    List<Checkin> findByGoalIdOrderByCheckinDateDesc(String goalId);
    boolean existsByGoalIdAndCheckinDate(String goalId, LocalDate date);
    List<Checkin> findByGoalIdAndCheckinDateBetweenOrderByCheckinDateAsc(
            String goalId, LocalDate from, LocalDate to);
}
