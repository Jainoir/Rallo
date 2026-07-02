package com.rallo.auth.security;

import com.rallo.auth.model.Role;
import com.rallo.auth.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private static final String SECRET = "test-secret-that-is-at-least-32-characters!!";

    private JwtUtil jwtUtil;
    private User user;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiryMs", 900_000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenExpiryMs", 604_800_000L);

        user = new User();
        user.setId("user-1");
        user.setUsername("jane");
        user.setRoles(Set.of(Role.USER));
    }

    @Test
    void accessTokenRoundTripPreservesClaims() {
        String token = jwtUtil.generateAccessToken(user);

        Claims claims = jwtUtil.parseToken(token);
        assertThat(claims.getSubject()).isEqualTo("user-1");
        assertThat(claims.get("username")).isEqualTo("jane");
        assertThat(claims.get("roles")).isNotNull();
    }

    @Test
    void extractUserIdReturnsSubject() {
        String token = jwtUtil.generateRefreshToken(user);

        assertThat(jwtUtil.extractUserId(token)).isEqualTo("user-1");
    }

    @Test
    void rejectsTokenSignedWithDifferentSecret() {
        JwtUtil other = new JwtUtil();
        ReflectionTestUtils.setField(other, "secret", "another-secret-that-is-32-characters-long!!");
        ReflectionTestUtils.setField(other, "accessTokenExpiryMs", 900_000L);
        ReflectionTestUtils.setField(other, "refreshTokenExpiryMs", 604_800_000L);
        String forged = other.generateAccessToken(user);

        assertThatThrownBy(() -> jwtUtil.parseToken(forged))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void rejectsExpiredToken() {
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiryMs", -1_000L);
        String expired = jwtUtil.generateAccessToken(user);

        assertThatThrownBy(() -> jwtUtil.parseToken(expired))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
