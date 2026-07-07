package com.rallo.notification.repository;

import com.rallo.notification.model.GoalActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface GoalActivityRepository extends JpaRepository<GoalActivity, String> {
    List<GoalActivity> findByFrequencyAndLastCheckinDate(String frequency, LocalDate lastCheckinDate);
    List<GoalActivity> findByLastCheckinDateBeforeAndCurrentStreakGreaterThan(LocalDate date, int streak);
}
