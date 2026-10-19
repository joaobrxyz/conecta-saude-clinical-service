package com.example.clinical_service.dto;

import java.time.LocalDateTime;

public record ExamSummaryDTO(
        String id,
        String title,
        String fileName,
        String contentType,
        long size,
        LocalDateTime uploadedAt
) {}
