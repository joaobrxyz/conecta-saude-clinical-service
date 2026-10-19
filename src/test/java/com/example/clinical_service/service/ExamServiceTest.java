package com.example.clinical_service.service;

import com.example.clinical_service.client.IdentityClient;
import com.example.clinical_service.model.Exam;
import com.example.clinical_service.repository.ExamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {
    @Mock ExamRepository repository;
    @Mock org.springframework.data.mongodb.gridfs.GridFsTemplate gridFs;
    @Mock IdentityClient identityClient;
    @InjectMocks ExamService service;

    @Test void rejectsUnsupportedFileTypeBeforeStorage() {
        var file = new MockMultipartFile("file", "result.txt", "text/plain", "result".getBytes());
        assertThatThrownBy(() -> service.upload(UUID.randomUUID(), UUID.randomUUID(), "Resultado", file))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("PDF, JPG ou PNG");
    }

    @Test void deniesPatientAccessToAnotherPatientsExam() {
        Exam exam = new Exam(); exam.setPatientId(UUID.randomUUID());
        when(repository.findById("exam-id")).thenReturn(Optional.of(exam));
        assertThatThrownBy(() -> service.requireAccessible("exam-id", UUID.randomUUID(), false))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }
}
