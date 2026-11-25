package com.vasudha.referral_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReferralRequestCreateDTO {

    @NotNull
    private Long receiverId;

    @NotBlank
    private String company;

    @NotBlank
    private String jobTitle;

    private String message;

    private String resumeLink;
}
