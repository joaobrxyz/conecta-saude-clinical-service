package com.example.clinical_service.control;

import com.example.clinical_service.dto.ExamSummaryDTO;
import com.example.clinical_service.model.Exam;
import com.example.clinical_service.service.ExamService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/exams")
public class ExamController {
    private final ExamService service;

    public ExamController(ExamService service) { this.service = service; }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<ExamSummaryDTO> upload(@RequestParam UUID patientId, @RequestParam String title,
                                                  @RequestParam MultipartFile file, HttpServletRequest request) {
        UUID doctorId = UUID.fromString((String) request.getAttribute("userId"));
        return ResponseEntity.status(201).body(service.upload(doctorId, patientId, title, file));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('MEDICO') or (hasRole('PACIENTE') and #patientId.toString() == authentication.name)")
    public List<ExamSummaryDTO> list(@PathVariable UUID patientId) {
        return service.list(patientId);
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('MEDICO', 'PACIENTE')")
    public ResponseEntity<org.springframework.core.io.Resource> download(@PathVariable String id, HttpServletRequest request) throws IOException {
        UUID userId = UUID.fromString((String) request.getAttribute("userId"));
        boolean doctor = request.isUserInRole("MEDICO") || org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MEDICO"));
        Exam exam = service.requireAccessible(id, userId, doctor);
        var download = service.download(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(exam.getContentType()))
                .contentLength(exam.getSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(exam.getFileName(), StandardCharsets.UTF_8).build().toString())
                .body(download.resource());
    }
}
