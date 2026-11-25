package com.vasudha.referral_backend.repository;

import com.vasudha.referral_backend.entity.ReferralRequest;
import com.vasudha.referral_backend.entity.ReferralStatus;
import com.vasudha.referral_backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReferralRequestRepository extends JpaRepository<ReferralRequest, Long> {

    @EntityGraph(attributePaths = {"requester", "receiver"})
    Page<ReferralRequest> findByRequester(User requester, Pageable pageable);

    @EntityGraph(attributePaths = {"requester", "receiver"})
    Page<ReferralRequest> findByReceiver(User receiver, Pageable pageable);

    boolean existsByRequesterIdAndReceiverIdAndCompanyIgnoreCaseAndJobTitleIgnoreCaseAndStatus(
            Long requesterId,
            Long receiverId,
            String company,
            String jobTitle,
            ReferralStatus status
    );
}
