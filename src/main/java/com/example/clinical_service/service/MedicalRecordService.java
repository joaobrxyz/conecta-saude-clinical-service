package com.example.clinical_service.service;

import com.example.clinical_service.client.AppointmentClient;
import com.example.clinical_service.client.IdentityClient;
import com.example.clinical_service.dto.DoctorInfoDTO;
import com.example.clinical_service.dto.MedicalRecordRequestDTO;
import com.example.clinical_service.dto.MedicalRecordSummaryDTO;
import com.example.clinical_service.dto.PrescriptionResponseDTO;
import com.example.clinical_service.model.MedicalRecord;
import com.example.clinical_service.model.Prescription;
import com.example.clinical_service.model.Vitals;
import com.example.clinical_service.repository.MedicalRecordRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MedicalRecordService {

    @Autowired
    private MedicalRecordRepository repository;

    @Autowired
    private IdentityClient identityClient;

    @Autowired
    private AppointmentClient appointmentClient;

    // ==========================================
    // 1. GESTÃO CENTRAL DE PRONTUÁRIOS
    // ==========================================

    public MedicalRecordSummaryDTO criarRegisto(UUID doctorId, MedicalRecordRequestDTO request) {

        if (repository.existsByAppointmentId(request.appointmentId())) {
            throw new IllegalArgumentException("Já existe um registo clínico guardado para esta consulta.");
        }

        DoctorInfoDTO doctorInfo;

        try {
            doctorInfo = identityClient.getDoctorById(doctorId);
            identityClient.getPatientById(request.patientId());
            appointmentClient.getAppointmentById(request.appointmentId());
        } catch (FeignException.NotFound e) {
            throw new IllegalArgumentException("Erro de validação: Médico, paciente ou consulta não encontrados no sistema.");
        }

        Vitals vitals = null;
        if (request.vitals() != null) {
            vitals = new Vitals(
                    request.vitals().weightKg(),
                    request.vitals().heightMeters(),
                    request.vitals().bloodPressure()
            );
        }

        List<Prescription> prescriptions = null;
        if (request.prescriptions() != null && !request.prescriptions().isEmpty()) {
            prescriptions = request.prescriptions().stream()
                    .map(p -> new Prescription(
                            p.medicationName(),
                            p.dosage(),
                            p.instructions(),
                            p.validUntil()
                    )).toList();
        }

        MedicalRecord record = new MedicalRecord(request, doctorId, vitals, prescriptions, doctorInfo.name(), doctorInfo.specialty());

        MedicalRecord saved = repository.save(record);

        return new MedicalRecordSummaryDTO(
                saved.getId(),
                saved.getCreatedAt(),
                saved.getRecordTitle(),
                doctorInfo.name(),
                doctorInfo.specialty(),
                saved.getClinicalDescription()
        );
    }

    public List<MedicalRecordSummaryDTO> listarHistoricoPaciente(UUID patientId) {
        List<MedicalRecord> records = repository.findAllByPatientIdOrderByCreatedAtDesc(patientId);

        return records.stream().map(record -> new MedicalRecordSummaryDTO(
                record.getId(),
                record.getCreatedAt(),
                record.getRecordTitle(),
                record.getDoctorName(),
                record.getDoctorSpecialty(),
                record.getClinicalDescription()
        )).toList();
    }

    public MedicalRecord buscarPorId(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prontuário não encontrado no sistema."));
    }

    public MedicalRecord buscarPorAgendamento(UUID appointmentId) {
        return repository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Nenhum registo clínico encontrado para esta consulta."));
    }

    public void adicionarAdendo(String id, String addendumText) {
        MedicalRecord record = buscarPorId(id);
        String currentNotes = record.getPrivateNotes() != null ? record.getPrivateNotes() : "";

        // Concatena o novo adendo mantendo o histórico de notas
        record.setPrivateNotes(currentNotes + "\n\n[Adendo em " + LocalDateTime.now() + "]: " + addendumText);
        repository.save(record);
    }

    // ==========================================
    // 2. SINAIS VITAIS
    // ==========================================

    public Vitals buscarUltimosSinaisVitais(UUID patientId) {
        List<MedicalRecord> records = repository.findAllByPatientIdOrderByCreatedAtDesc(patientId);

        // Procura a primeira consulta que tenha os sinais vitais preenchidos
        return records.stream()
                .map(MedicalRecord::getVitals)
                .filter(vitals -> vitals != null)
                .findFirst()
                .orElse(null);
    }

    // ==========================================
    // 3. PRESCRIÇÕES (RECEITUÁRIO)
    // ==========================================

    public List<PrescriptionResponseDTO> listarPrescricoesPaciente(UUID patientId) {
        List<MedicalRecord> records = repository.findAllByPatientIdOrderByCreatedAtDesc(patientId);

        return records.stream()
                .filter(record -> record.getPrescriptions() != null && !record.getPrescriptions().isEmpty())
                .flatMap(record -> record.getPrescriptions().stream().map(p -> {
                    // Se a data de validade não for anterior a hoje, está ativa
                    boolean isActive = !p.getValidUntil().isBefore(LocalDate.now());

                    return new PrescriptionResponseDTO(
                            p.getMedicationName(),
                            p.getDosage(),
                            p.getInstructions(),
                            record.getDoctorName(),
                            record.getDoctorSpecialty(),
                            record.getCreatedAt().toLocalDate(),
                            p.getValidUntil(),
                            isActive
                    );
                }))
                .toList();
    }

    public void solicitarRenovacao(UUID prescriptionId) {
        // Num cenário real, isto publicaria uma mensagem num broker (Kafka/RabbitMQ)
        // para notificar o médico no notification-service.
        System.out.println("Solicitação de renovação gerada para a receita: " + prescriptionId);
    }

    // ==========================================
    // 4. EXPORTAÇÃO (PDF)
    // ==========================================

    public byte[] gerarPdfHistorico(UUID patientId) {
        // Para exportar PDFs reais em Java, a biblioteca "OpenPDF" ou "iText" é necessária.
        // Como não temos isso configurado no pom.xml ainda, retornamos um mock de bytes.
        String textoFake = "PDF de Histórico do Paciente " + patientId;
        return textoFake.getBytes();
    }
}