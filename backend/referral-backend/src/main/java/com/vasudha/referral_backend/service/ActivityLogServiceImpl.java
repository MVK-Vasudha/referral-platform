package com.vasudha.referral_backend.service;

import com.vasudha.referral_backend.entity.*;
import com.vasudha.referral_backend.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Override
    public void log(Long actorId,
                    Long targetId,
                    String type,
                    String metadata) {

        ActivityLog log = ActivityLog.builder()
                .actorId(actorId)
                .targetId(targetId)
                .type(ActivityType.valueOf(type))
                .metadata(metadata)
                .build();

        activityLogRepository.save(log);
    }
}