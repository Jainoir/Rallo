package com.rallo.checkin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rallo.checkin.client.AuthClient;
import com.rallo.checkin.dto.LeaderboardEntry;
import com.rallo.checkin.model.Frequency;
import com.rallo.checkin.model.Goal;
import com.rallo.checkin.repository.GoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocialStreakServiceTest {

    private static final ZoneOffset UTC = ZoneOffset.UTC;

    @Mock
    private AuthClient authClient;

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private StreakService streakService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private SocialStreakService service;

    @BeforeEach
    void setUp() {
        service = new SocialStreakService(
                authClient, goalRepository, streakService, redisTemplate, new ObjectMapper());
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    private Goal goal(String id) {
        Goal goal = new Goal();
        goal.setId(id);
        goal.setFrequency(Frequency.DAILY);
        return goal;
    }

    @Test
    void friendsLeaderboardIncludesSelfAndRanksByBestStreak() {
        when(authClient.listFriends("me")).thenReturn(List.of(
                new AuthClient.FriendDto("friend-1", "jane")));
        when(goalRepository.findByUserIdAndActiveTrue("me")).thenReturn(List.of(goal("g1")));
        when(goalRepository.findByUserIdAndActiveTrue("friend-1"))
                .thenReturn(List.of(goal("g2"), goal("g3")));
        when(streakService.currentStreak(any(Goal.class), eq(UTC)))
                .thenAnswer(inv -> switch (((Goal) inv.getArgument(0)).getId()) {
                    case "g1" -> 3;
                    case "g2" -> 1;
                    case "g3" -> 9;   // jane's best
                    default -> 0;
                });

        List<LeaderboardEntry> board = service.friendsLeaderboard("me", "sanjai", UTC);

        assertThat(board).containsExactly(
                new LeaderboardEntry("friend-1", "jane", 9),
                new LeaderboardEntry("me", "sanjai", 3));
    }

    @Test
    void usersWithoutGoalsScoreZero() {
        when(authClient.listFriends("me")).thenReturn(List.of());
        when(goalRepository.findByUserIdAndActiveTrue("me")).thenReturn(List.of());

        List<LeaderboardEntry> board = service.friendsLeaderboard("me", "sanjai", UTC);

        assertThat(board).containsExactly(new LeaderboardEntry("me", "sanjai", 0));
    }

    @Test
    void groupLeaderboardComputesAndCaches() {
        when(valueOperations.get(anyString())).thenReturn(null);
        when(authClient.groupMembers("group-1", "me")).thenReturn(List.of(
                new AuthClient.MemberDto("me", "sanjai"),
                new AuthClient.MemberDto("friend-1", "jane")));
        when(goalRepository.findByUserIdAndActiveTrue(anyString())).thenReturn(List.of());

        List<LeaderboardEntry> board = service.groupLeaderboard("group-1", "me", UTC);

        assertThat(board).extracting(LeaderboardEntry::username).containsExactly("jane", "sanjai");
    }

    @Test
    void groupLeaderboardServedFromCacheWhenPresent() {
        when(valueOperations.get(anyString()))
                .thenReturn("[{\"userId\":\"me\",\"username\":\"sanjai\",\"bestStreak\":5}]");

        List<LeaderboardEntry> board = service.groupLeaderboard("group-1", "me", UTC);

        assertThat(board).containsExactly(new LeaderboardEntry("me", "sanjai", 5));
    }

    @Test
    void redisOutageFallsBackToDirectComputation() {
        when(valueOperations.get(anyString()))
                .thenThrow(new RedisConnectionFailureException("no redis"));
        when(authClient.groupMembers("group-1", "me")).thenReturn(List.of(
                new AuthClient.MemberDto("me", "sanjai")));
        when(goalRepository.findByUserIdAndActiveTrue("me")).thenReturn(List.of());

        List<LeaderboardEntry> board = service.groupLeaderboard("group-1", "me", UTC);

        assertThat(board).containsExactly(new LeaderboardEntry("me", "sanjai", 0));
    }
}
