package com.vasudha.referral_backend.dto;

public record UserProfileResponse(
        Long id,
        String username,
        String fullName,
        String email,
        String headline,
        String bio,
        String experience,
        String company,
        String skills,
        String role,
        String createdAt,
        long connectionCount
) {}