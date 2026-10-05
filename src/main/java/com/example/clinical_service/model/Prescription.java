package com.example.clinical_service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Prescription {
    private String medicationName; // ex: "Losartana 50mg"
    private String dosage;         // ex: "1 comprimido - 1x ao dia"
    private String instructions;   // ex: "Tomar preferencialmente no mesmo horário..."
    private LocalDate validUntil;  // Validade da receita
}