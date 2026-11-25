package com.vasudha.referral_backend.service;

import com.vasudha.referral_backend.dto.ConnectionDTO;
import com.vasudha.referral_backend.entity.*;
import com.vasudha.referral_backend.exception.*;
import com.vasudha.referral_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ConnectionServiceImpl implements ConnectionService {

    private final ConnectionRequestRepository connectionRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    @Override
    public ConnectionDTO sendRequest(Long senderId, Long receiverId) {

        if (senderId.equals(receiverId)) {
            throw new BusinessConflictException("Cannot connect to yourself");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        Optional<ConnectionRequest> existingOpt =
                connectionRepository.findBetweenUsers(senderId, receiverId);

        if (existingOpt.isPresent()) {
            ConnectionRequest existing = existingOpt.get();

            if (existing.getStatus() == ConnectionStatus.ACCEPTED) {
                throw new BusinessConflictException("Users are already connected");
            }

            if (existing.getStatus() == ConnectionStatus.PENDING) {
                if (existing.getSender().getId().equals(senderId)) {
                    throw new BusinessConflictException("Connection request already sent");
                }
                throw new BusinessConflictException("This user has already sent you a connection request");
            }

            // Allow retrying after a rejected request, even if the opposite party initiates
            existing.setSender(sender);
            existing.setReceiver(receiver);
            existing.setStatus(ConnectionStatus.PENDING);
            ConnectionRequest saved = connectionRepository.save(existing);

            return toConnectionDTO(saved, receiver, saved.getUpdatedAt());
        }

        ConnectionRequest request = ConnectionRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(ConnectionStatus.PENDING)
                .build();

        ConnectionRequest saved = connectionRepository.save(request);
        return toConnectionDTO(saved, receiver, saved.getCreatedAt());
    }

    @Override
    public void acceptRequest(Long requestId, Long currentUserId) {

        ConnectionRequest request = connectionRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection request not found"));

        if (!request.getReceiver().getId().equals(currentUserId)) {
            throw new BusinessForbiddenException("Not authorized to accept this request");
        }

        if (request.getStatus() != ConnectionStatus.PENDING) {
            throw new BusinessConflictException("Connection request already processed");
        }

        request.setStatus(ConnectionStatus.ACCEPTED);

        activityLogService.log(
                currentUserId,
                request.getSender().getId(),
                ActivityType.CONNECTION_ACCEPTED.name(),
                "Connection ID: " + request.getId()
        );
    }

    @Override
    public void rejectRequest(Long requestId, Long currentUserId) {

        ConnectionRequest request = connectionRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection request not found"));

        if (!request.getReceiver().getId().equals(currentUserId)) {
            throw new BusinessForbiddenException("Not authorized to reject this request");
        }

        if (request.getStatus() != ConnectionStatus.PENDING) {
            throw new BusinessConflictException("Connection request already processed");
        }

        request.setStatus(ConnectionStatus.REJECTED);
    }

    @Override
    public void cancelOrRemoveConnection(Long requestId, Long currentUserId) {

        ConnectionRequest request = connectionRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection request not found"));

        boolean isSender = request.getSender().getId().equals(currentUserId);
        boolean isReceiver = request.getReceiver().getId().equals(currentUserId);

        if (request.getStatus() == ConnectionStatus.PENDING) {
            if (!isSender) {
                throw new BusinessForbiddenException("Only the sender can cancel a pending request");
            }
            connectionRepository.delete(request);
            return;
        }

        if (request.getStatus() == ConnectionStatus.ACCEPTED) {
            if (!isSender && !isReceiver) {
                throw new BusinessForbiddenException("Not authorized to remove this connection");
            }
            connectionRepository.delete(request);
            return;
        }

        throw new BusinessConflictException("Cannot cancel a rejected connection request");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConnectionDTO> getPendingRequests(Long userId, Pageable pageable) {

        return connectionRepository.findPendingRequests(userId, pageable)
                .map(cr -> toConnectionDTO(cr, cr.getSender(), effectiveTimestamp(cr)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConnectionDTO> getSentPendingRequests(Long userId, Pageable pageable) {

        return connectionRepository.findSentPendingRequests(userId, pageable)
                .map(cr -> toConnectionDTO(cr, cr.getReceiver(), effectiveTimestamp(cr)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConnectionDTO> getMutualConnections(Long currentUserId, Long targetUserId, Pageable pageable) {

        if (!userRepository.existsById(targetUserId)) {
            throw new ResourceNotFoundException("Target user not found");
        }

        if (currentUserId.equals(targetUserId)) {
            return Page.empty(pageable);
        }

        return connectionRepository.findMutualConnections(currentUserId, targetUserId, pageable)
                .map(user -> new ConnectionDTO(
                        null,
                        user.getId(),
                        user.getUsername(),
                        user.getFullName(),
                        user.getHeadline(),
                        user.getCompany(),
                        null
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConnectionDTO> getMyConnections(Long userId, Pageable pageable) {

        return connectionRepository.findAcceptedConnections(userId, pageable)
                .map(cr -> {
                    User other = cr.getSender().getId().equals(userId)
                            ? cr.getReceiver()
                            : cr.getSender();

                    return toConnectionDTO(cr, other, effectiveTimestamp(cr));
                });
    }

    private LocalDateTime effectiveTimestamp(ConnectionRequest cr) {
        return cr.getUpdatedAt() != null ? cr.getUpdatedAt() : cr.getCreatedAt();
    }

    private ConnectionDTO toConnectionDTO(ConnectionRequest cr, User user, LocalDateTime timestamp) {
        return new ConnectionDTO(
                cr.getId(),
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getHeadline(),
                user.getCompany(),
                timestamp
        );
    }
}