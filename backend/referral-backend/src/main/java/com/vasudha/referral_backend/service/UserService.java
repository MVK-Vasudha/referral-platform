package com.vasudha.referral_backend.service;

import com.vasudha.referral_backend.dto.UserProfileResponse;
import com.vasudha.referral_backend.dto.UserProfileUpdateRequest;
import com.vasudha.referral_backend.dto.UserSearchResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserProfileResponse getUserProfileByUsername(String username);

    UserProfileResponse updateUserProfileByUsername(
            String username,
            UserProfileUpdateRequest request
    );

    Page<UserSearchResponseDTO> searchUsers(
            String name,
            String skills,
            String company,
            String headline,
            Pageable pageable,
            Long currentUserId);
}
