package com.vasudha.referral_backend.controller;

import com.vasudha.referral_backend.dto.*;
import com.vasudha.referral_backend.service.ReferralService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/referrals")
@RequiredArgsConstructor
public class ReferralController {

    private final ReferralService referralService;

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<ReferralResponseDTO>> createReferral(
            @Valid @RequestBody ReferralRequestCreateDTO dto,
            Authentication authentication) {

        ReferralResponseDTO response =
                referralService.createReferral(dto, authentication.getName());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        "Referral request created successfully",
                        response
                ));
    }

    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<Page<ReferralResponseDTO>>> getSent(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        Page<ReferralResponseDTO> page =
                referralService.getSentReferrals(authentication.getName(), pageable);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Sent referrals fetched successfully",
                        page
                )
        );
    }

    @GetMapping("/received")
    public ResponseEntity<ApiResponse<Page<ReferralResponseDTO>>> getReceived(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        Page<ReferralResponseDTO> page =
                referralService.getReceivedReferrals(authentication.getName(), pageable);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Received referrals fetched successfully",
                        page
                )
        );
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<ReferralResponseDTO>> accept(
            @PathVariable Long id,
            Authentication authentication) {

        ReferralResponseDTO response =
                referralService.acceptReferral(id, authentication.getName());

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Referral accepted successfully",
                        response
                )
        );
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ReferralResponseDTO>> reject(
            @PathVariable Long id,
            Authentication authentication) {

        ReferralResponseDTO response =
                referralService.rejectReferral(id, authentication.getName());

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Referral rejected successfully",
                        response
                )
        );
    }
}
