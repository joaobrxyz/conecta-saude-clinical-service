package com.example.clinical_service.dto;

import java.util.UUID;

public record DoctorInfoDTO(
        UUID id,
        String name,
        String specialty
) {}