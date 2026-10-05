package com.example.clinical_service.dto;

import java.time.LocalDate;

public record PrescriptionResponseDTO(
        String medicationName,
        String dosage,
        String instructions,
        String doctorName,
        String doctorSpecialty,
        LocalDate issueDate,
        LocalDate validUntil,
        boolean isActive
) {}