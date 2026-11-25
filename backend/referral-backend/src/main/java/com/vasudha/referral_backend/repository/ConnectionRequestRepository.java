package com.vasudha.referral_backend.repository;

import com.vasudha.referral_backend.entity.ConnectionRequest;
import com.vasudha.referral_backend.entity.ConnectionStatus;
import com.vasudha.referral_backend.entity.User;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConnectionRequestRepository
        extends JpaRepository<ConnectionRequest, Long> {

    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);

    boolean existsBySenderIdAndReceiverIdAndStatus(
            Long senderId, Long receiverId, ConnectionStatus status
    );

    @EntityGraph(attributePaths = {"sender", "receiver"})
    @Query("""
    SELECT cr FROM ConnectionRequest cr
    WHERE (cr.sender.id = :firstUserId AND cr.receiver.id = :secondUserId)
       OR (cr.sender.id = :secondUserId AND cr.receiver.id = :firstUserId)
""")
    Optional<ConnectionRequest> findBetweenUsers(
            @Param("firstUserId") Long firstUserId,
            @Param("secondUserId") Long secondUserId
    );

    @EntityGraph(attributePaths = {"sender", "receiver"})
    @Query("""
    SELECT cr FROM ConnectionRequest cr
    WHERE (cr.sender.id = :currentUserId AND cr.receiver.id IN :targetUserIds)
       OR (cr.receiver.id = :currentUserId AND cr.sender.id IN :targetUserIds)
""")
    List<ConnectionRequest> findRequestsBetweenUserAndTargets(
            @Param("currentUserId") Long currentUserId,
            @Param("targetUserIds") List<Long> targetUserIds
    );

    @Query("""
    SELECT CASE WHEN COUNT(cr) > 0 THEN true ELSE false END
    FROM ConnectionRequest cr
    WHERE ((cr.sender.id = :firstUserId AND cr.receiver.id = :secondUserId)
        OR (cr.sender.id = :secondUserId AND cr.receiver.id = :firstUserId))
      AND cr.status = 'ACCEPTED'
""")
    boolean existsAcceptedConnectionBetween(
            @Param("firstUserId") Long firstUserId,
            @Param("secondUserId") Long secondUserId
    );

    @EntityGraph(attributePaths = {"sender", "receiver"})
    @Query("""
    SELECT cr FROM ConnectionRequest cr
    WHERE (cr.sender.id = :userId OR cr.receiver.id = :userId)
    AND cr.status = 'ACCEPTED'
""")
    Page<ConnectionRequest> findAcceptedConnections(
            @Param("userId") Long userId,
            Pageable pageable
    );


    @EntityGraph(attributePaths = {"sender", "receiver"})
    @Query("""
    SELECT cr FROM ConnectionRequest cr
    WHERE cr.receiver.id = :userId
    AND cr.status = 'PENDING'
""")
    Page<ConnectionRequest> findPendingRequests(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"sender", "receiver"})
    @Query("""
    SELECT cr FROM ConnectionRequest cr
    WHERE cr.sender.id = :userId
    AND cr.status = 'PENDING'
""")
    Page<ConnectionRequest> findSentPendingRequests(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("""
    SELECT u FROM User u
    WHERE u.id IN (
        SELECT CASE
            WHEN cr1.sender.id = :currentUserId THEN cr1.receiver.id
            ELSE cr1.sender.id
        END
        FROM ConnectionRequest cr1
        WHERE (cr1.sender.id = :currentUserId OR cr1.receiver.id = :currentUserId)
        AND cr1.status = 'ACCEPTED'
    )
    AND u.id IN (
        SELECT CASE
            WHEN cr2.sender.id = :targetUserId THEN cr2.receiver.id
            ELSE cr2.sender.id
        END
        FROM ConnectionRequest cr2
        WHERE (cr2.sender.id = :targetUserId OR cr2.receiver.id = :targetUserId)
        AND cr2.status = 'ACCEPTED'
    )
""")
    Page<User> findMutualConnections(
            @Param("currentUserId") Long currentUserId,
            @Param("targetUserId") Long targetUserId,
            Pageable pageable
    );

    @Query("""
    SELECT COUNT(cr)
    FROM ConnectionRequest cr
    WHERE (cr.sender.id = :userId OR cr.receiver.id = :userId)
    AND cr.status = 'ACCEPTED'
""")
    Long countConnections(@Param("userId") Long userId);

    Optional<ConnectionRequest> findById(Long id);
}
