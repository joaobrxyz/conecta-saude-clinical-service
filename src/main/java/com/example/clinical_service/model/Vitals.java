package com.example.clinical_service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vitals {
    private Double weightKg;
    private Double heightMeters;
    private String bloodPressure; // ex: "120/80"
}