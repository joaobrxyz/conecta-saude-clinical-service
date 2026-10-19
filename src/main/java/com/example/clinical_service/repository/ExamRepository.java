package com.example.clinical_service.repository;

import com.example.clinical_service.model.Exam;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface ExamRepository extends MongoRepository<Exam, String> {
    List<Exam> findAllByPatientIdOrderByUploadedAtDesc(UUID patientId);
}
