package com.example.clinical_service.service;

import com.example.clinical_service.client.IdentityClient;
import com.example.clinical_service.dto.ExamSummaryDTO;
import com.example.clinical_service.model.Exam;
import com.example.clinical_service.repository.ExamRepository;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ExamService {
    private static final long MAX_FILE_SIZE = 8L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("application/pdf", "image/jpeg", "image/png");

    private final ExamRepository repository;
    private final GridFsTemplate gridFs;
    private final IdentityClient identityClient;

    public ExamService(ExamRepository repository, GridFsTemplate gridFs, IdentityClient identityClient) {
        this.repository = repository;
        this.gridFs = gridFs;
        this.identityClient = identityClient;
    }

    public ExamSummaryDTO upload(UUID doctorId, UUID patientId, String title, MultipartFile file) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Informe o título do exame.");
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Selecione um arquivo para o exame.");
        if (file.getSize() > MAX_FILE_SIZE) throw new IllegalArgumentException("O arquivo deve ter no máximo 8 MB.");
        String contentType = file.getContentType();
        if (!ALLOWED_TYPES.contains(contentType)) throw new IllegalArgumentException("Envie um arquivo PDF, JPG ou PNG.");
        identityClient.getPatientById(patientId);

        String filename = safeFileName(file.getOriginalFilename());
        try {
            ObjectId gridFsId = gridFs.store(file.getInputStream(), filename, contentType, new Document("patientId", patientId.toString()));
            Exam exam = new Exam(null, patientId, doctorId, title.trim(), filename, contentType, file.getSize(), gridFsId.toHexString(), LocalDateTime.now());
            return summary(repository.save(exam));
        } catch (IOException error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não foi possível ler o arquivo enviado.");
        }
    }

    public List<ExamSummaryDTO> list(UUID patientId) {
        return repository.findAllByPatientIdOrderByUploadedAtDesc(patientId).stream().map(this::summary).toList();
    }

    public Download download(String id) {
        Exam exam = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exame não encontrado."));
        GridFSFile file = gridFs.findOne(org.springframework.data.mongodb.core.query.Query.query(org.springframework.data.mongodb.core.query.Criteria.where("_id").is(new ObjectId(exam.getGridFsId()))));
        if (file == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo do exame não encontrado.");
        GridFsResource resource = gridFs.getResource(file);
        return new Download(exam, resource);
    }

    public Exam requireAccessible(String id, UUID userId, boolean doctor) {
        Exam exam = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exame não encontrado."));
        if (!doctor && !exam.getPatientId().equals(userId)) throw new org.springframework.security.access.AccessDeniedException("Você não tem acesso a este exame.");
        return exam;
    }

    private ExamSummaryDTO summary(Exam exam) {
        return new ExamSummaryDTO(exam.getId(), exam.getTitle(), exam.getFileName(), exam.getContentType(), exam.getSize(), exam.getUploadedAt());
    }

    private String safeFileName(String filename) {
        String value = filename == null ? "exame" : filename.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return value.isBlank() ? "exame" : value.substring(0, Math.min(value.length(), 120));
    }

    public record Download(Exam exam, GridFsResource resource) {}
}
