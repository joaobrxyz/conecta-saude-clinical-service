package com.example.clinical_service.model;

import com.example.clinical_service.dto.MedicalRecordRequestDTO;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Document(collection = "medical_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecord {

    @Id
    private String id;
    private UUID patientId;
    private UUID doctorId;
    private String doctorName;
    private String doctorSpecialty;
    private UUID appointmentId;

    // Cabeçalho do Card (ex: "Avaliação Nutricional e Esportiva")
    private String recordTitle;

    // Descrição visível para o paciente
    private String clinicalDescription;

    // Anotações invisíveis para o paciente (só o médico vê)
    private String privateNotes;

    // Sinais Vitais medidos no dia
    private Vitals vitals;

    // Lista de prescrições estruturadas
    private List<Prescription> prescriptions;

    private LocalDateTime createdAt;

    public MedicalRecord(MedicalRecordRequestDTO dto, UUID doctorId, Vitals vitals, List<Prescription> prescriptions, String doctorName, String doctorSpecialty) {
        this.patientId = dto.patientId();
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.doctorSpecialty = doctorSpecialty;
        this.appointmentId = dto.appointmentId();
        this.recordTitle = dto.recordTitle();
        this.clinicalDescription = dto.clinicalDescription();
        this.privateNotes = dto.privateNotes();
        this.vitals = vitals;
        this.prescriptions = prescriptions;
        this.createdAt = LocalDateTime.now();
    }
}