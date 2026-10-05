package com.example.clinical_service.control;

import com.example.clinical_service.dto.*;
import com.example.clinical_service.model.MedicalRecord;
import com.example.clinical_service.model.Vitals;
import com.example.clinical_service.service.MedicalRecordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/records")
public class MedicalRecordController {

    @Autowired
    private MedicalRecordService service;

    // ==========================================
    // 1. GESTÃO CENTRAL DE PRONTUÁRIOS
    // ==========================================

    @PostMapping
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<MedicalRecordSummaryDTO> criarRegisto(
            @RequestBody @Valid MedicalRecordRequestDTO request,
            HttpServletRequest servletRequest) {
        String doctorIdStr = (String) servletRequest.getAttribute("userId");
        UUID doctorId = UUID.fromString(doctorIdStr);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarRegisto(doctorId, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDICO', 'PACIENTE')")
    public ResponseEntity<MedicalRecord> buscarProntuarioDetalhado(@PathVariable String id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('MEDICO', 'PACIENTE')")
    public ResponseEntity<List<MedicalRecordSummaryDTO>> listarHistoricoPaciente(@PathVariable UUID patientId) {
        return ResponseEntity.ok(service.listarHistoricoPaciente(patientId));
    }

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('MEDICO', 'PACIENTE')")
    public ResponseEntity<MedicalRecord> buscarProntuarioDaConsulta(@PathVariable UUID appointmentId) {
        return ResponseEntity.ok(service.buscarPorAgendamento(appointmentId));
    }

    @PatchMapping("/{id}/addendum")
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<Void> adicionarAdendo(@PathVariable String id, @RequestBody String addendumText) {
        service.adicionarAdendo(id, addendumText);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 2. SINAIS VITAIS
    // ==========================================

    @GetMapping("/patient/{patientId}/vitals/latest")
    @PreAuthorize("hasAnyRole('MEDICO', 'PACIENTE')")
    public ResponseEntity<Vitals> buscarUltimosSinaisVitais(@PathVariable UUID patientId) {
        return ResponseEntity.ok(service.buscarUltimosSinaisVitais(patientId));
    }

    // ==========================================
    // 3. PRESCRIÇÕES (RECEITUÁRIO)
    // ==========================================

    @GetMapping("/patient/{patientId}/prescriptions")
    @PreAuthorize("hasAnyRole('MEDICO', 'PACIENTE')")
    public ResponseEntity<List<PrescriptionResponseDTO>> listarPrescricoesPaciente(@PathVariable UUID patientId) {
        return ResponseEntity.ok(service.listarPrescricoesPaciente(patientId));
    }

    @PostMapping("/prescriptions/{prescriptionId}/renew")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<Void> solicitarRenovacaoReceita(@PathVariable UUID prescriptionId) {
        // Envia uma notificação/mensagem para o médico aprovar uma nova receita
        service.solicitarRenovacao(prescriptionId);
        return ResponseEntity.accepted().build();
    }

    // ==========================================
    // 4. EXPORTAÇÃO (PDF)
    // ==========================================

    @GetMapping("/patient/{patientId}/export")
    @PreAuthorize("hasAnyRole('MEDICO', 'PACIENTE')")
    public ResponseEntity<byte[]> exportarHistoricoCompletoPDF(@PathVariable UUID patientId) {
        // Retorna um array de bytes configurado com cabeçalho "application/pdf"
        byte[] pdf = service.gerarPdfHistorico(patientId);
        return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=historico.pdf").body(pdf);
    }
}