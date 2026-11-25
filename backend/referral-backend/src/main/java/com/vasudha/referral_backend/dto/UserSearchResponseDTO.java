package com.vasudha.referral_backend.dto;

public record UserSearchResponseDTO(
        Long id,
        String username,
        String fullName,
        String headline,
        String company,
        String skills,
        String connectionStatus,
        Long connectionRequestId
) {}
