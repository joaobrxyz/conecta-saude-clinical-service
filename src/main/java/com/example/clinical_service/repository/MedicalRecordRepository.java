package com.example.clinical_service.repository;

import com.example.clinical_service.model.MedicalRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicalRecordRepository extends MongoRepository<MedicalRecord, String> {

    boolean existsByAppointmentId(UUID appointmentId);

    List<MedicalRecord> findAllByPatientIdOrderByCreatedAtDesc(UUID patientId);

    Optional<MedicalRecord> findByAppointmentId(UUID appointmentId);
}