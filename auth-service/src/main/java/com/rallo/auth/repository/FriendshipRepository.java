package com.rallo.auth.repository;

import com.rallo.auth.model.Friendship;
import com.rallo.auth.model.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, String> {

    /** A pair is linked if a request exists in either direction, any status. */
    @Query("""
            select f from Friendship f
            where (f.requesterId = :a and f.addresseeId = :b)
               or (f.requesterId = :b and f.addresseeId = :a)
            """)
    Optional<Friendship> findBetween(@Param("a") String userA, @Param("b") String userB);

    List<Friendship> findByAddresseeIdAndStatus(String addresseeId, FriendshipStatus status);

    Optional<Friendship> findByIdAndAddresseeIdAndStatus(String id, String addresseeId, FriendshipStatus status);

    @Query("""
            select f from Friendship f
            where f.status = com.rallo.auth.model.FriendshipStatus.ACCEPTED
              and (f.requesterId = :userId or f.addresseeId = :userId)
            """)
    List<Friendship> findAcceptedFor(@Param("userId") String userId);
}
