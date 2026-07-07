package com.rallo.checkin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rallo.checkin.client.AuthClient;
import com.rallo.checkin.dto.LeaderboardEntry;
import com.rallo.checkin.model.Goal;
import com.rallo.checkin.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cross-service composition: the auth service owns who your friends and
 * group mates are; this service owns their streaks. Leaderboards join the
 * two here, with Redis as a best-effort cache — when Redis is unavailable
 * (e.g. not deployed yet) the leaderboard is simply computed directly.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SocialStreakService {

    private static final Duration CACHE_TTL = Duration.ofSeconds(60);

    private final AuthClient authClient;
    private final GoalRepository goalRepository;
    private final StreakService streakService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public List<LeaderboardEntry> friendsLeaderboard(String userId, String username, ZoneId zone) {
        Map<String, String> participants = new LinkedHashMap<>();
        participants.put(userId, username);
        authClient.listFriends(userId)
                .forEach(friend -> participants.put(friend.userId(), friend.username()));
        return rank(participants, zone);
    }

    public List<LeaderboardEntry> groupLeaderboard(String groupId, String userId, ZoneId zone) {
        String cacheKey = "leaderboard:group:%s:%s".formatted(groupId, LocalDate.now(zone));

        List<LeaderboardEntry> cached = readCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        Map<String, String> participants = new LinkedHashMap<>();
        authClient.groupMembers(groupId, userId)
                .forEach(member -> participants.put(member.userId(), member.username()));
        List<LeaderboardEntry> leaderboard = rank(participants, zone);

        writeCache(cacheKey, leaderboard);
        return leaderboard;
    }

    private List<LeaderboardEntry> rank(Map<String, String> participants, ZoneId zone) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        participants.forEach((id, name) -> entries.add(new LeaderboardEntry(id, name, bestStreak(id, zone))));
        entries.sort(Comparator.comparingInt(LeaderboardEntry::bestStreak).reversed()
                .thenComparing(LeaderboardEntry::username));
        return entries;
    }

    /** A user's leaderboard score: their best current streak across active goals. */
    private int bestStreak(String userId, ZoneId zone) {
        return goalRepository.findByUserIdAndActiveTrue(userId).stream()
                .mapToInt(goal -> streakService.currentStreak(goal, zone))
                .max()
                .orElse(0);
    }

    private List<LeaderboardEntry> readCache(String key) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            return json == null ? null
                    : objectMapper.readValue(json, new TypeReference<List<LeaderboardEntry>>() {});
        } catch (Exception e) {
            log.debug("Leaderboard cache read skipped: {}", e.getMessage());
            return null;
        }
    }

    private void writeCache(String key, List<LeaderboardEntry> leaderboard) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(leaderboard), CACHE_TTL);
        } catch (Exception e) {
            log.debug("Leaderboard cache write skipped: {}", e.getMessage());
        }
    }
}
