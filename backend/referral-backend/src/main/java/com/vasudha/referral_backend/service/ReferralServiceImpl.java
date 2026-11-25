package com.vasudha.referral_backend.service;

import com.vasudha.referral_backend.dto.*;
import com.vasudha.referral_backend.entity.*;
import com.vasudha.referral_backend.exception.*;
import com.vasudha.referral_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReferralServiceImpl implements ReferralService {

    private final ReferralRequestRepository referralRepository;
    private final ConnectionRequestRepository connectionRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    @Override
    @Transactional
    public ReferralResponseDTO createReferral(ReferralRequestCreateDTO dto, String username) {

        User requester = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Requester not found"));

        User receiver = userRepository.findById(dto.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        if (requester.getId().equals(receiver.getId())) {
            throw new BusinessConflictException("Cannot refer yourself");
        }

        if (!connectionRepository.existsAcceptedConnectionBetween(requester.getId(), receiver.getId())) {
            throw new BusinessForbiddenException("You must be connected with this user to request a referral");
        }

        String company = dto.getCompany().trim();
        String jobTitle = dto.getJobTitle().trim();

        if (referralRepository.existsByRequesterIdAndReceiverIdAndCompanyIgnoreCaseAndJobTitleIgnoreCaseAndStatus(
                requester.getId(),
                receiver.getId(),
                company,
                jobTitle,
                ReferralStatus.PENDING
        )) {
            throw new BusinessConflictException("A pending referral request for this role already exists");
        }

        ReferralRequest referral = ReferralRequest.builder()
                .requester(requester)
                .receiver(receiver)
                .company(company)
                .jobTitle(jobTitle)
                .message(dto.getMessage())
                .resumeLink(dto.getResumeLink())
                .build();

        referralRepository.save(referral);

        activityLogService.log(
                requester.getId(),
                receiver.getId(),
                ActivityType.REFERRAL_CREATED.name(),
                "Referral for " + company
        );

        return mapToDTO(referral);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReferralResponseDTO> getSentReferrals(String username, Pageable pageable) {

        User requester = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return referralRepository.findByRequester(requester, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReferralResponseDTO> getReceivedReferrals(String username, Pageable pageable) {

        User receiver = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return referralRepository.findByReceiver(receiver, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional
    public ReferralResponseDTO acceptReferral(Long id, String username) {
        return updateStatus(id, username, ReferralStatus.ACCEPTED);
    }

    @Override
    @Transactional
    public ReferralResponseDTO rejectReferral(Long id, String username) {
        return updateStatus(id, username, ReferralStatus.REJECTED);
    }

    private ReferralResponseDTO updateStatus(Long id, String username, ReferralStatus status) {

        ReferralRequest referral = referralRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Referral not found"));

        if (!referral.getReceiver().getUsername().equals(username)) {
            throw new BusinessForbiddenException("Not authorized to modify this referral");
        }

        if (referral.getStatus() != ReferralStatus.PENDING) {
            throw new BusinessConflictException("Referral already processed");
        }

        referral.setStatus(status);

        // 🔵 LOG ACCEPT / REJECT
        ActivityType type = (status == ReferralStatus.ACCEPTED)
                ? ActivityType.REFERRAL_ACCEPTED
                : ActivityType.REFERRAL_REJECTED;

        activityLogService.log(
                referral.getReceiver().getId(),
                referral.getRequester().getId(),
                type.name(),
                "Referral ID: " + referral.getId()
        );

        return mapToDTO(referral);
    }

    private ReferralResponseDTO mapToDTO(ReferralRequest referral) {

        return ReferralResponseDTO.builder()
                .id(referral.getId())
                .requesterId(referral.getRequester().getId())
                .requesterUsername(referral.getRequester().getUsername())
                .receiverId(referral.getReceiver().getId())
                .receiverUsername(referral.getReceiver().getUsername())
                .company(referral.getCompany())
                .jobTitle(referral.getJobTitle())
                .message(referral.getMessage())
                .resumeLink(referral.getResumeLink())
                .status(referral.getStatus())
                .createdAt(referral.getCreatedAt())
                .updatedAt(referral.getUpdatedAt())
                .build();
    }
}