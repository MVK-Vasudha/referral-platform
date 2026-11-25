package com.vasudha.referral_backend.service;

import com.vasudha.referral_backend.dto.ConnectionDTO;
import com.vasudha.referral_backend.entity.ConnectionRequest;
import com.vasudha.referral_backend.entity.ConnectionStatus;
import com.vasudha.referral_backend.entity.Role;
import com.vasudha.referral_backend.entity.User;
import com.vasudha.referral_backend.exception.BusinessConflictException;
import com.vasudha.referral_backend.exception.BusinessForbiddenException;
import com.vasudha.referral_backend.exception.ResourceNotFoundException;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConnectionServiceImplTest {

    @Mock
    private ConnectionRequestRepository connectionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityLogService activityLogService;

    @InjectMocks
    private ConnectionServiceImpl connectionService;

    @Test
    void sendRequest_allowsRetryAfterRejectionInEitherDirection() {
        User alice = User.builder().id(1L).username("alice").fullName("Alice").role(Role.USER).build();
        User bob = User.builder().id(2L).username("bob").fullName("Bob").role(Role.USER).build();

        ConnectionRequest rejected = ConnectionRequest.builder()
                .id(50L)
                .sender(alice)
                .receiver(bob)
                .status(ConnectionStatus.REJECTED)
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(bob));
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(connectionRepository.findBetweenUsers(2L, 1L)).thenReturn(Optional.of(rejected));
        when(connectionRepository.save(rejected)).thenReturn(rejected);

        ConnectionDTO result = connectionService.sendRequest(2L, 1L);

        assertThat(rejected.getStatus()).isEqualTo(ConnectionStatus.PENDING);
        assertThat(rejected.getSender()).isEqualTo(bob);
        assertThat(rejected.getReceiver()).isEqualTo(alice);
        assertThat(result.id()).isEqualTo(50L);
        assertThat(result.userId()).isEqualTo(1L);
    }

    @Test
    void sendRequest_blocksDuplicatePendingOrAcceptedRequests() {
        User alice = User.builder().id(1L).username("alice").role(Role.USER).build();
        User bob = User.builder().id(2L).username("bob").role(Role.USER).build();
        ConnectionRequest pending = ConnectionRequest.builder()
                .id(12L)
                .sender(alice)
                .receiver(bob)
                .status(ConnectionStatus.PENDING)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(userRepository.findById(2L)).thenReturn(Optional.of(bob));
        when(connectionRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> connectionService.sendRequest(1L, 2L))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("already sent");
    }

    @Test
    void cancelOrRemoveConnection_enforcesOwnershipAndStatusRules() {
        User alice = User.builder().id(1L).username("alice").role(Role.USER).build();
        User bob = User.builder().id(2L).username("bob").role(Role.USER).build();

        ConnectionRequest pending = ConnectionRequest.builder()
                .id(77L)
                .sender(alice)
                .receiver(bob)
                .status(ConnectionStatus.PENDING)
                .build();

        when(connectionRepository.findById(77L)).thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> connectionService.cancelOrRemoveConnection(77L, 2L))
                .isInstanceOf(BusinessForbiddenException.class);

        connectionService.cancelOrRemoveConnection(77L, 1L);
        verify(connectionRepository).delete(pending);
    }

    @Test
    void getMutualConnections_usesBothCurrentAndTargetUsers() {
        User mutual = User.builder()
                .id(3L)
                .username("charlie")
                .fullName("Charlie")
                .headline("Engineer")
                .company("Acme")
                .role(Role.USER)
                .build();

        when(userRepository.existsById(2L)).thenReturn(true);
        when(connectionRepository.findMutualConnections(1L, 2L, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(mutual)));

        Page<ConnectionDTO> result = connectionService.getMutualConnections(1L, 2L, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).userId()).isEqualTo(3L);
        assertThat(result.getContent().get(0).username()).isEqualTo("charlie");

        when(userRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> connectionService.getMutualConnections(1L, 99L, PageRequest.of(0, 10)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
