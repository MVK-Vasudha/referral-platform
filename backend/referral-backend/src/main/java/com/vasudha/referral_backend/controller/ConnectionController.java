package com.vasudha.referral_backend.controller;

import com.vasudha.referral_backend.dto.ApiResponse;
import com.vasudha.referral_backend.dto.ConnectionDTO;
import com.vasudha.referral_backend.entity.User;
import com.vasudha.referral_backend.exception.ResourceNotFoundException;
import com.vasudha.referral_backend.repository.UserRepository;
import com.vasudha.referral_backend.service.ConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/connections")
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionService connectionService;
    private final UserRepository userRepository;

    private Long getCurrentUserId(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return user.getId();
    }

    private void validatePagination(int page, int size) {
        if (page < 0 || size <= 0 || size > 50) {
            throw new IllegalArgumentException("Invalid pagination parameters");
        }
    }

    @PostMapping("/request/{userId}")
    public ResponseEntity<ApiResponse<ConnectionDTO>> sendRequest(
            Authentication authentication,
            @PathVariable Long userId
    ) {

        Long currentUserId = getCurrentUserId(authentication);
        ConnectionDTO created = connectionService.sendRequest(currentUserId, userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        "Connection request sent",
                        created
                ));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<Void>> accept(
            Authentication authentication,
            @PathVariable Long id
    ) {

        Long currentUserId = getCurrentUserId(authentication);
        connectionService.acceptRequest(id, currentUserId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Connection accepted",
                        null
                )
        );
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(
            Authentication authentication,
            @PathVariable Long id
    ) {

        Long currentUserId = getCurrentUserId(authentication);
        connectionService.rejectRequest(id, currentUserId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Connection rejected",
                        null
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelOrRemove(
            Authentication authentication,
            @PathVariable Long id
    ) {

        Long currentUserId = getCurrentUserId(authentication);
        connectionService.cancelOrRemoveConnection(id, currentUserId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Connection removed",
                        null
                )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<ConnectionDTO>>> myConnections(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        validatePagination(page, size);
        Long currentUserId = getCurrentUserId(authentication);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<ConnectionDTO> connections =
                connectionService.getMyConnections(currentUserId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Connections fetched successfully",
                        connections
                )
        );
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<Page<ConnectionDTO>>> getPendingRequests(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        validatePagination(page, size);
        Long currentUserId = getCurrentUserId(authentication);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<ConnectionDTO> pending =
                connectionService.getPendingRequests(currentUserId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Pending connection requests fetched successfully",
                        pending
                )
        );
    }

    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<Page<ConnectionDTO>>> getSentPendingRequests(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        validatePagination(page, size);
        Long currentUserId = getCurrentUserId(authentication);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<ConnectionDTO> sent =
                connectionService.getSentPendingRequests(currentUserId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Sent connection requests fetched successfully",
                        sent
                )
        );
    }

    @GetMapping("/{userId}/mutual")
    public ResponseEntity<ApiResponse<Page<ConnectionDTO>>> getMutualConnections(
            Authentication authentication,
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        validatePagination(page, size);
        Long currentUserId = getCurrentUserId(authentication);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "id")
        );

        Page<ConnectionDTO> mutual =
                connectionService.getMutualConnections(currentUserId, userId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Mutual connections fetched successfully",
                        mutual
                )
        );
    }
}