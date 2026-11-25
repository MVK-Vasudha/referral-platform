package com.vasudha.referral_backend.controller;

import com.vasudha.referral_backend.dto.*;
import com.vasudha.referral_backend.entity.User;
import com.vasudha.referral_backend.exception.ResourceNotFoundException;
import com.vasudha.referral_backend.repository.UserRepository;
import com.vasudha.referral_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    private Long getCurrentUserId(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return user.getId();
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            Authentication authentication
    ) {

        UserProfileResponse response =
                userService.getUserProfileByUsername(authentication.getName());

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Profile fetched successfully",
                        response
                )
        );
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {

        UserProfileResponse response =
                userService.updateUserProfileByUsername(authentication.getName(), request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Profile updated successfully",
                        response
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<UserSearchResponseDTO>>> searchUsers(
            Authentication authentication,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String headline,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        if (page < 0 || size <= 0 || size > 50) {
            throw new IllegalArgumentException("Invalid pagination parameters");
        }

        Long currentUserId = getCurrentUserId(authentication);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "id")
        );

        Page<UserSearchResponseDTO> results =
                userService.searchUsers(
                        name,
                        skills,
                        company,
                        headline,
                        pageable,
                        currentUserId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Users fetched successfully",
                        results
                )
        );
    }
}
