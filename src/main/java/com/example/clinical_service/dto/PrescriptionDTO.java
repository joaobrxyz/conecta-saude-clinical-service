package com.example.clinical_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PrescriptionDTO(
        @NotBlank(message = "O nome do medicamento é obrigatório")
        String medicationName,

        @NotBlank(message = "A dosagem é obrigatória")
        String dosage,

        @NotBlank(message = "As instruções são obrigatórias")
        String instructions,

        @NotNull(message = "A data de validade é obrigatória")
        LocalDate validUntil
) {}