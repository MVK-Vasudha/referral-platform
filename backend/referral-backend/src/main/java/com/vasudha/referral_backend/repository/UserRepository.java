package com.vasudha.referral_backend.repository;

import com.vasudha.referral_backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);

    @Query("""
        SELECT u
        FROM User u
        WHERE u.id <> :currentUserId
        AND (:name IS NULL
             OR LOWER(COALESCE(u.fullName, '')) LIKE LOWER(CONCAT('%', :name, '%'))
             OR LOWER(u.username) LIKE LOWER(CONCAT('%', :name, '%')))
        AND (:skills IS NULL
             OR LOWER(COALESCE(u.skills, '')) LIKE LOWER(CONCAT('%', :skills, '%')))
        AND (:company IS NULL
             OR LOWER(COALESCE(u.company, '')) LIKE LOWER(CONCAT('%', :company, '%')))
        AND (:headline IS NULL
             OR LOWER(COALESCE(u.headline, '')) LIKE LOWER(CONCAT('%', :headline, '%')))
    """)
    Page<User> searchUsers(
            @Param("name") String name,
            @Param("skills") String skills,
            @Param("company") String company,
            @Param("headline") String headline,
            @Param("currentUserId") Long currentUserId,
            Pageable pageable
    );
}
