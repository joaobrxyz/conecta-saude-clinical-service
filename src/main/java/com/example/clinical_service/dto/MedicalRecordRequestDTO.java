package com.example.clinical_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record MedicalRecordRequestDTO(
        @NotNull(message = "O ID do paciente é obrigatório")
        UUID patientId,

        @NotNull(message = "O ID da consulta é obrigatório")
        UUID appointmentId,

        @NotBlank(message = "O título do prontuário é obrigatório")
        String recordTitle,

        @NotBlank(message = "A descrição clínica é obrigatória")
        String clinicalDescription,

        String privateNotes,

        VitalsDTO vitals,

        @Valid
        List<PrescriptionDTO> prescriptions
) {}