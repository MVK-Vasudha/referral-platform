package com.vasudha.referral_backend.service;

public interface ActivityLogService {

    void log(Long actorId,
             Long targetId,
             String type,
             String metadata);
}