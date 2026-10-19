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
        return ResponseEntity.ok(com.example.clinical_service.security.RecordAccess.visible(service.buscarPorId(id)));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('MEDICO') or (hasRole('PACIENTE') and #patientId.toString() == authentication.name)")
    public ResponseEntity<List<MedicalRecordSummaryDTO>> listarHistoricoPaciente(@PathVariable UUID patientId) {
        return ResponseEntity.ok(service.listarHistoricoPaciente(patientId));
    }

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('MEDICO', 'PACIENTE')")
    public ResponseEntity<MedicalRecord> buscarProntuarioDaConsulta(@PathVariable UUID appointmentId) {
        return ResponseEntity.ok(com.example.clinical_service.security.RecordAccess.visible(service.buscarPorAgendamento(appointmentId)));
    }

    @PatchMapping("/{id}/addendum")
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<Void> adicionarAdendo(@PathVariable String id, @RequestBody String addendumText) {
        com.example.clinical_service.security.RecordAccess.requireAuthor(service.buscarPorId(id));
        service.adicionarAdendo(id, addendumText);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 2. SINAIS VITAIS
    // ==========================================

    @GetMapping("/patient/{patientId}/vitals/latest")
    @PreAuthorize("hasRole('MEDICO') or (hasRole('PACIENTE') and #patientId.toString() == authentication.name)")
    public ResponseEntity<Vitals> buscarUltimosSinaisVitais(@PathVariable UUID patientId) {
        return ResponseEntity.ok(service.buscarUltimosSinaisVitais(patientId));
    }

    // ==========================================
    // 3. PRESCRIÇÕES (RECEITUÁRIO)
    // ==========================================

    @GetMapping("/patient/{patientId}/prescriptions")
    @PreAuthorize("hasRole('MEDICO') or (hasRole('PACIENTE') and #patientId.toString() == authentication.name)")
    public ResponseEntity<List<PrescriptionResponseDTO>> listarPrescricoesPaciente(@PathVariable UUID patientId) {
        return ResponseEntity.ok(service.listarPrescricoesPaciente(patientId));
    }

    @PostMapping("/prescriptions/{prescriptionId}/renew")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<Void> solicitarRenovacaoReceita(@PathVariable UUID prescriptionId) {
        // Envia uma notificação/mensagem para o médico aprovar uma nova receita
        throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Renovação de receitas ainda não disponível.");

    }

    // ==========================================
    // 4. EXPORTAÇÃO (PDF)
    // ==========================================

    @GetMapping("/patient/{patientId}/export")
    @PreAuthorize("hasRole('MEDICO') or (hasRole('PACIENTE') and #patientId.toString() == authentication.name)")
    public ResponseEntity<byte[]> exportarHistoricoCompletoPDF(@PathVariable UUID patientId) {
        // Retorna um array de bytes configurado com cabeçalho "application/pdf"
        throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Exportação PDF ainda não disponível.");
    }
}