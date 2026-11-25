package com.vasudha.referral_backend.service;

import com.vasudha.referral_backend.dto.ConnectionDTO;
import org.springframework.data.domain.*;

public interface ConnectionService {

    ConnectionDTO sendRequest(Long senderId, Long receiverId);

    void acceptRequest(Long requestId, Long currentUserId);

    void rejectRequest(Long requestId, Long currentUserId);

    void cancelOrRemoveConnection(Long requestId, Long currentUserId);

    Page<ConnectionDTO> getPendingRequests(Long userId, Pageable pageable);

    Page<ConnectionDTO> getSentPendingRequests(Long userId, Pageable pageable);

    Page<ConnectionDTO> getMutualConnections(Long currentUserId, Long targetUserId, Pageable pageable);

    Page<ConnectionDTO> getMyConnections(Long userId, Pageable pageable);
}
