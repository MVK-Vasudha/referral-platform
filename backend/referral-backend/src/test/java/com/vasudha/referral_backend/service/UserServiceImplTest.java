package com.vasudha.referral_backend.service;

import com.vasudha.referral_backend.dto.UserProfileResponse;
import com.vasudha.referral_backend.dto.UserProfileUpdateRequest;
import com.vasudha.referral_backend.dto.UserSearchResponseDTO;
import com.vasudha.referral_backend.entity.ConnectionRequest;
import com.vasudha.referral_backend.entity.ConnectionStatus;
import com.vasudha.referral_backend.entity.Role;
import com.vasudha.referral_backend.entity.User;
import com.vasudha.referral_backend.repository.ConnectionRequestRepository;
import com.vasudha.referral_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ConnectionRequestRepository connectionRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void updateUserProfile_persistsBioAndExperienceAndPreservesOmittedFields() {
        User user = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@example.com")
                .password("encoded")
                .role(Role.USER)
                .fullName("Alice Smith")
                .headline("Staff Engineer")
                .company("Acme")
                .createdAt(null)
                .build();

        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setBio("Mentoring backend engineers.");
        request.setExperience("8 years building distributed systems.");
        request.setSkills("Java, Spring Boot");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(connectionRepository.countConnections(1L)).thenReturn(3L);

        UserProfileResponse response = userService.updateUserProfileByUsername("alice", request);

        assertThat(response.bio()).isEqualTo("Mentoring backend engineers.");
        assertThat(response.experience()).isEqualTo("8 years building distributed systems.");
        assertThat(response.skills()).isEqualTo("Java, Spring Boot");
        assertThat(response.fullName()).isEqualTo("Alice Smith");
        assertThat(response.company()).isEqualTo("Acme");
        assertThat(response.createdAt()).isNull();
        assertThat(response.connectionCount()).isEqualTo(3L);
        verify(userRepository).save(user);
    }

    @Test
    void searchUsers_normalizesBlankFiltersToNullAndMapsConnectionStatuses() {
        User currentUser = User.builder().id(1L).username("me").role(Role.USER).build();
        User bob = User.builder().id(2L).username("bob").fullName(null).role(Role.USER).build();
        User carol = User.builder().id(3L).username("carol").fullName("Carol").role(Role.USER).build();
        User dave = User.builder().id(4L).username("dave").fullName("Dave").role(Role.USER).build();
        User eve = User.builder().id(5L).username("eve").fullName("Eve").role(Role.USER).build();

        Page<User> page = new PageImpl<>(List.of(bob, carol, dave, eve), PageRequest.of(0, 10), 4);
        when(userRepository.searchUsers(isNull(), eq("Java"), isNull(), isNull(), eq(1L), any(Pageable.class)))
                .thenReturn(page);

        ConnectionRequest pendingSent = ConnectionRequest.builder()
                .id(10L)
                .sender(currentUser)
                .receiver(carol)
                .status(ConnectionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        ConnectionRequest pendingReceived = ConnectionRequest.builder()
                .id(11L)
                .sender(dave)
                .receiver(currentUser)
                .status(ConnectionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        ConnectionRequest accepted = ConnectionRequest.builder()
                .id(12L)
                .sender(currentUser)
                .receiver(eve)
                .status(ConnectionStatus.ACCEPTED)
                .createdAt(LocalDateTime.now())
                .build();

        when(connectionRepository.findRequestsBetweenUserAndTargets(1L, List.of(2L, 3L, 4L, 5L)))
                .thenReturn(List.of(pendingSent, pendingReceived, accepted));

        Page<UserSearchResponseDTO> results = userService.searchUsers(
                "   ",
                " Java ",
                "",
                null,
                PageRequest.of(0, 10),
                1L
        );

        assertThat(results.getContent()).hasSize(4);
        assertThat(results.getContent().get(0).connectionStatus()).isEqualTo("NONE");
        assertThat(results.getContent().get(0).username()).isEqualTo("bob");
        assertThat(results.getContent().get(1).connectionStatus()).isEqualTo("PENDING_SENT");
        assertThat(results.getContent().get(1).connectionRequestId()).isEqualTo(10L);
        assertThat(results.getContent().get(2).connectionStatus()).isEqualTo("PENDING_RECEIVED");
        assertThat(results.getContent().get(2).connectionRequestId()).isEqualTo(11L);
        assertThat(results.getContent().get(3).connectionStatus()).isEqualTo("ACCEPTED");
    }
}
