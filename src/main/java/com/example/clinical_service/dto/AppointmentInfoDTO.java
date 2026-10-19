package com.example.clinical_service.dto;

import java.util.UUID;

public record AppointmentInfoDTO(UUID id, UUID doctorId, UUID patientId, String status) {}
