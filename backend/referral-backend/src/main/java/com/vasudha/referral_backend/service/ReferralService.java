package com.vasudha.referral_backend.service;

import com.vasudha.referral_backend.dto.ReferralRequestCreateDTO;
import com.vasudha.referral_backend.dto.ReferralResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReferralService {

    ReferralResponseDTO createReferral(ReferralRequestCreateDTO dto, String currentUsername);

    Page<ReferralResponseDTO> getSentReferrals(String username, Pageable pageable);

    Page<ReferralResponseDTO> getReceivedReferrals(String username, Pageable pageable);

    ReferralResponseDTO acceptReferral(Long id, String username);

    ReferralResponseDTO rejectReferral(Long id, String username);
}
