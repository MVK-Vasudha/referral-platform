package com.vasudha.referral_backend.service;

import com.vasudha.referral_backend.dto.ReferralRequestCreateDTO;
import com.vasudha.referral_backend.dto.ReferralResponseDTO;
import com.vasudha.referral_backend.entity.ReferralRequest;
import com.vasudha.referral_backend.entity.ReferralStatus;
import com.vasudha.referral_backend.entity.Role;
import com.vasudha.referral_backend.entity.User;
import com.vasudha.referral_backend.exception.BusinessConflictException;
import com.vasudha.referral_backend.exception.BusinessForbiddenException;
import com.vasudha.referral_backend.repository.ConnectionRequestRepository;
import com.vasudha.referral_backend.repository.ReferralRequestRepository;
import com.vasudha.referral_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReferralServiceImplTest {

    @Mock
    private ReferralRequestRepository referralRepository;

    @Mock
    private ConnectionRequestRepository connectionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityLogService activityLogService;

    @InjectMocks
    private ReferralServiceImpl referralService;

    @Test
    void createReferral_requiresAcceptedConnection() {
        User alice = User.builder().id(1L).username("alice").role(Role.USER).build();
        User bob = User.builder().id(2L).username("bob").role(Role.USER).build();

        ReferralRequestCreateDTO dto = new ReferralRequestCreateDTO();
        dto.setReceiverId(2L);
        dto.setCompany("Acme");
        dto.setJobTitle("Backend Engineer");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(userRepository.findById(2L)).thenReturn(Optional.of(bob));
        when(connectionRepository.existsAcceptedConnectionBetween(1L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> referralService.createReferral(dto, "alice"))
                .isInstanceOf(BusinessForbiddenException.class)
                .hasMessageContaining("must be connected");
    }

    @Test
    void createReferral_blocksDuplicatePendingReferralForSameCompanyAndRole() {
        User alice = User.builder().id(1L).username("alice").role(Role.USER).build();
        User bob = User.builder().id(2L).username("bob").role(Role.USER).build();

        ReferralRequestCreateDTO dto = new ReferralRequestCreateDTO();
        dto.setReceiverId(2L);
        dto.setCompany(" Acme ");
        dto.setJobTitle(" Backend Engineer ");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(userRepository.findById(2L)).thenReturn(Optional.of(bob));
        when(connectionRepository.existsAcceptedConnectionBetween(1L, 2L)).thenReturn(true);
        when(referralRepository.existsByRequesterIdAndReceiverIdAndCompanyIgnoreCaseAndJobTitleIgnoreCaseAndStatus(
                1L, 2L, "Acme", "Backend Engineer", ReferralStatus.PENDING))
                .thenReturn(true);

        assertThatThrownBy(() -> referralService.createReferral(dto, "alice"))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createReferral_savesTrimmedFieldsWhenConnectedAndUnique() {
        User alice = User.builder().id(1L).username("alice").role(Role.USER).build();
        User bob = User.builder().id(2L).username("bob").role(Role.USER).build();

        ReferralRequestCreateDTO dto = new ReferralRequestCreateDTO();
        dto.setReceiverId(2L);
        dto.setCompany(" Acme ");
        dto.setJobTitle(" Backend Engineer ");
        dto.setMessage("Please refer me");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(userRepository.findById(2L)).thenReturn(Optional.of(bob));
        when(connectionRepository.existsAcceptedConnectionBetween(1L, 2L)).thenReturn(true);
        when(referralRepository.existsByRequesterIdAndReceiverIdAndCompanyIgnoreCaseAndJobTitleIgnoreCaseAndStatus(
                1L, 2L, "Acme", "Backend Engineer", ReferralStatus.PENDING))
                .thenReturn(false);
        when(referralRepository.save(any(ReferralRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReferralResponseDTO response = referralService.createReferral(dto, "alice");

        assertThat(response.getCompany()).isEqualTo("Acme");
        assertThat(response.getJobTitle()).isEqualTo("Backend Engineer");
        assertThat(response.getRequesterUsername()).isEqualTo("alice");
        assertThat(response.getReceiverUsername()).isEqualTo("bob");
        verify(activityLogService).log(1L, 2L, "REFERRAL_CREATED", "Referral for Acme");
    }
}
