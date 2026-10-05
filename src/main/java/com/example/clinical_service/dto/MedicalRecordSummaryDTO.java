package com.example.clinical_service.dto;

import java.time.LocalDateTime;

public record MedicalRecordSummaryDTO(
        String id,
        LocalDateTime date,
        String recordTitle,
        String doctorName,
        String doctorSpecialty,
        String clinicalDescription
) {}