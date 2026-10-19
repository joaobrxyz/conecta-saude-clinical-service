package com.example.clinical_service.dto;

import java.util.UUID;

public record DoctorInfoDTO(
        UUID id,
        @com.fasterxml.jackson.annotation.JsonAlias("nome") String name,
        @com.fasterxml.jackson.annotation.JsonAlias("especialidade") String specialty
) {}