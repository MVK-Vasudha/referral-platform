package com.vasudha.referral_backend.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileUpdateRequest {

    @Size(max = 255)
    private String fullName;

    @Size(max = 255)
    private String headline;

    @Size(max = 2000)
    private String bio;

    @Size(max = 1000)
    private String skills;

    @Size(max = 2000)
    private String experience;

    @Size(max = 255)
    private String company;
}
