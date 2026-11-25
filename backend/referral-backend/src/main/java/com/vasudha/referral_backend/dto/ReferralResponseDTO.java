package com.vasudha.referral_backend.dto;

import com.vasudha.referral_backend.entity.ReferralStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReferralResponseDTO {

    private Long id;

    private Long requesterId;
    private String requesterUsername;

    private Long receiverId;
    private String receiverUsername;

    private String company;
    private String jobTitle;
    private String message;
    private String resumeLink;

    private ReferralStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
