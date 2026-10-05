package com.example.clinical_service.dto;

public record VitalsDTO(
        Double weightKg,
        Double heightMeters,
        String bloodPressure
) {}