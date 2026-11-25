package com.vasudha.referral_backend.dto;

import java.time.LocalDateTime;

public record ConnectionDTO(
        Long id,
        Long userId,
        String username,
        String fullName,
        String headline,
        String company,
        LocalDateTime connectedAt
) {}
