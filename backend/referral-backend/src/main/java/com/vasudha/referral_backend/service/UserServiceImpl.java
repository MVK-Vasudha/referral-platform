package com.vasudha.referral_backend.service;

import com.vasudha.referral_backend.dto.UserProfileResponse;
import com.vasudha.referral_backend.dto.UserProfileUpdateRequest;
import com.vasudha.referral_backend.dto.UserSearchResponseDTO;
import com.vasudha.referral_backend.entity.ConnectionRequest;
import com.vasudha.referral_backend.entity.ConnectionStatus;
import com.vasudha.referral_backend.entity.User;
import com.vasudha.referral_backend.exception.ResourceNotFoundException;
import com.vasudha.referral_backend.repository.ConnectionRequestRepository;
import com.vasudha.referral_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ConnectionRequestRepository connectionRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfileByUsername(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapToProfileResponse(user);
    }

    @Override
    public UserProfileResponse updateUserProfileByUsername(
            String username,
            UserProfileUpdateRequest request
    ) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getHeadline() != null) {
            user.setHeadline(request.getHeadline());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getExperience() != null) {
            user.setExperience(request.getExperience());
        }
        if (request.getCompany() != null) {
            user.setCompany(request.getCompany());
        }
        if (request.getSkills() != null) {
            user.setSkills(request.getSkills());
        }

        userRepository.save(user);

        return mapToProfileResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserSearchResponseDTO> searchUsers(
            String name,
            String skills,
            String company,
            String headline,
            Pageable pageable,
            Long currentUserId) {

        Pageable stablePageable = pageable.getSort().isUnsorted()
                ? PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "id")
                )
                : pageable;

        Page<User> usersPage = userRepository.searchUsers(
                trimToNull(name),
                trimToNull(skills),
                trimToNull(company),
                trimToNull(headline),
                currentUserId,
                stablePageable
        );

        List<Long> targetIds = usersPage.getContent().stream()
                .map(User::getId)
                .toList();

        Map<Long, ConnectionRequest> requestsByTargetId = targetIds.isEmpty()
                ? Collections.emptyMap()
                : connectionRepository.findRequestsBetweenUserAndTargets(currentUserId, targetIds)
                        .stream()
                        .collect(Collectors.toMap(
                                cr -> cr.getSender().getId().equals(currentUserId)
                                        ? cr.getReceiver().getId()
                                        : cr.getSender().getId(),
                                Function.identity(),
                                (first, second) -> first
                        ));

        return usersPage.map(user -> mapToSearchDTO(user, currentUserId, requestsByTargetId.get(user.getId())));
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        long connectionCount = connectionRepository.countConnections(user.getId());
        String createdAt = user.getCreatedAt() != null ? user.getCreatedAt().toString() : null;

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getHeadline(),
                user.getBio(),
                user.getExperience(),
                user.getCompany(),
                user.getSkills(),
                user.getRole().name(),
                createdAt,
                connectionCount
        );
    }

    private UserSearchResponseDTO mapToSearchDTO(
            User user,
            Long currentUserId,
            ConnectionRequest relation
    ) {
        String connectionStatus = "NONE";
        Long connectionRequestId = null;

        if (relation != null) {
            connectionRequestId = relation.getId();
            if (relation.getStatus() == ConnectionStatus.ACCEPTED) {
                connectionStatus = "ACCEPTED";
            } else if (relation.getStatus() == ConnectionStatus.REJECTED) {
                connectionStatus = "REJECTED";
            } else if (relation.getStatus() == ConnectionStatus.PENDING) {
                connectionStatus = relation.getSender().getId().equals(currentUserId)
                        ? "PENDING_SENT"
                        : "PENDING_RECEIVED";
            }
        }

        return new UserSearchResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getHeadline(),
                user.getCompany(),
                user.getSkills(),
                connectionStatus,
                connectionRequestId
        );
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}