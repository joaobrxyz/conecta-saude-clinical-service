package com.example.clinical_service.service;

import com.example.clinical_service.client.AppointmentClient;
import com.example.clinical_service.client.IdentityClient;
import com.example.clinical_service.dto.DoctorInfoDTO;
import com.example.clinical_service.dto.MedicalRecordRequestDTO;
import com.example.clinical_service.dto.MedicalRecordSummaryDTO;
import com.example.clinical_service.dto.PrescriptionDTO;
import com.example.clinical_service.dto.PrescriptionResponseDTO;
import com.example.clinical_service.dto.VitalsDTO;
import com.example.clinical_service.model.MedicalRecord;
import com.example.clinical_service.model.Prescription;
import com.example.clinical_service.model.Vitals;
import com.example.clinical_service.repository.MedicalRecordRepository;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {

    @InjectMocks
    MedicalRecordService medicalRecordService;

    @Mock
    MedicalRecordRepository repository;

    @Mock
    IdentityClient identityClient;

    @Mock
    AppointmentClient appointmentClient;

    // --- MÉTODOS AUXILIARES ---
    private MedicalRecordRequestDTO criarRequestDTO() {
        return new MedicalRecordRequestDTO(
                UUID.randomUUID(), // patientId
                UUID.randomUUID(), // appointmentId
                "Consulta de Rotina",
                "Paciente apresenta melhoras.",
                "Nota privada do médico",
                new VitalsDTO(75.0, 1.80, "120/80"),
                List.of(new PrescriptionDTO("Losartana", "50mg", "1x ao dia", LocalDate.now().plusDays(30)))
        );
    }

    private MedicalRecord criarMedicalRecord(UUID patientId) {
        MedicalRecord record = new MedicalRecord();
        record.setId("mongo-id-123");
        record.setPatientId(patientId != null ? patientId : UUID.randomUUID());
        record.setAppointmentId(UUID.randomUUID());
        record.setDoctorId(UUID.randomUUID());
        record.setRecordTitle("Consulta de Rotina");
        record.setClinicalDescription("Paciente apresenta melhoras.");
        record.setDoctorName("Dr. João Silva");
        record.setDoctorSpecialty("Cardiologia");
        record.setCreatedAt(LocalDateTime.now());
        record.setVitals(new Vitals(75.0, 1.80, "120/80"));
        record.setPrescriptions(List.of(
                new Prescription("Losartana", "50mg", "1x ao dia", LocalDate.now().plusDays(30))
        ));
        return record;
    }

    // ==========================================
    // 1. CRIAR REGISTO
    // ==========================================
    @DisplayName("Quando criar um registo clínico")
    @Nested
    class CriarRegisto {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado um request válido, deve mapear os dados e salvar no banco")
            @Test
            void teste1() {
                // Dado
                UUID doctorId = UUID.randomUUID();
                var dto = criarRequestDTO();
                var doctorInfo = new DoctorInfoDTO(doctorId, "Dr. João Silva", "Cardiologia");

                when(repository.existsByAppointmentId(dto.appointmentId())).thenReturn(false);
                when(identityClient.getDoctorById(doctorId)).thenReturn(doctorInfo);

                // Simula o save retornando a entidade com ID gerado
                MedicalRecord savedRecord = criarMedicalRecord(dto.patientId());
                when(repository.save(any(MedicalRecord.class))).thenReturn(savedRecord);

                // Quando
                MedicalRecordSummaryDTO atual = medicalRecordService.criarRegisto(doctorId, dto);

                // Então
                assertThat(atual).isNotNull();
                assertThat(atual.recordTitle()).isEqualTo("Consulta de Rotina");
                assertThat(atual.doctorName()).isEqualTo("Dr. João Silva");

                // Garante que fez as validações externas
                verify(identityClient, times(1)).getDoctorById(doctorId);
                verify(identityClient, times(1)).getPatientById(dto.patientId());
                verify(appointmentClient, times(1)).getAppointmentById(dto.appointmentId());

                // Garante que salvou no banco
                verify(repository, times(1)).save(any(MedicalRecord.class));
            }
        }

        @DisplayName("Então deve lançar erro de validação")
        @Nested
        class Falha {

            @DisplayName("Dado que já existe um prontuário para a consulta informada")
            @Test
            void teste1() {
                // Dado
                UUID doctorId = UUID.randomUUID();
                var dto = criarRequestDTO();

                when(repository.existsByAppointmentId(dto.appointmentId())).thenReturn(true);

                // Quando / Então
                assertThatThrownBy(() -> medicalRecordService.criarRegisto(doctorId, dto))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Já existe um registo clínico guardado para esta consulta.");

                verify(repository, never()).save(any());
            }

            @DisplayName("Dado que o médico, paciente ou consulta não existem no sistema (Erro Feign 404)")
            @Test
            void teste2() {
                // Dado
                UUID doctorId = UUID.randomUUID();
                var dto = criarRequestDTO();

                when(repository.existsByAppointmentId(dto.appointmentId())).thenReturn(false);
                when(identityClient.getDoctorById(doctorId)).thenThrow(mock(FeignException.NotFound.class));

                // Quando / Então
                assertThatThrownBy(() -> medicalRecordService.criarRegisto(doctorId, dto))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Erro de validação: Médico, paciente ou consulta não encontrados no sistema.");

                verify(repository, never()).save(any());
            }
        }
    }

    // ==========================================
    // 2. BUSCAR POR ID
    // ==========================================
    @DisplayName("Quando buscar prontuário por ID")
    @Nested
    class BuscarPorId {

        @DisplayName("Dado um ID existente, deve retornar o documento completo")
        @Test
        void sucesso() {
            String recordId = "mongo-id-123";
            MedicalRecord record = criarMedicalRecord(null);

            when(repository.findById(recordId)).thenReturn(Optional.of(record));

            MedicalRecord atual = medicalRecordService.buscarPorId(recordId);

            assertThat(atual).isNotNull();
            assertThat(atual.getId()).isEqualTo(recordId);
        }

        @DisplayName("Dado um ID inexistente, deve lançar exceção")
        @Test
        void falha() {
            String recordId = "id-invalido";
            when(repository.findById(recordId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> medicalRecordService.buscarPorId(recordId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Prontuário não encontrado no sistema.");
        }
    }

    // ==========================================
    // 3. BUSCAR SINAIS VITAIS
    // ==========================================
    @DisplayName("Quando buscar últimos sinais vitais")
    @Nested
    class BuscarSinaisVitais {

        @DisplayName("Dado um paciente com histórico, deve retornar o sinal vital mais recente")
        @Test
        void sucesso() {
            UUID patientId = UUID.randomUUID();
            MedicalRecord record = criarMedicalRecord(patientId);

            when(repository.findAllByPatientIdOrderByCreatedAtDesc(patientId)).thenReturn(List.of(record));

            Vitals vitals = medicalRecordService.buscarUltimosSinaisVitais(patientId);

            assertThat(vitals).isNotNull();
            assertThat(vitals.getBloodPressure()).isEqualTo("120/80");
        }

        @DisplayName("Dado um paciente sem histórico, deve retornar null")
        @Test
        void falha() {
            UUID patientId = UUID.randomUUID();
            when(repository.findAllByPatientIdOrderByCreatedAtDesc(patientId)).thenReturn(List.of());

            Vitals vitals = medicalRecordService.buscarUltimosSinaisVitais(patientId);

            assertThat(vitals).isNull();
        }
    }

    // ==========================================
    // 4. LISTAR PRESCRIÇÕES
    // ==========================================
    @DisplayName("Quando listar prescrições do paciente")
    @Nested
    class ListarPrescricoes {

        @DisplayName("Dado um paciente com prontuários, deve mapear e extrair receitas ativas e inativas")
        @Test
        void sucesso() {
            UUID patientId = UUID.randomUUID();
            MedicalRecord record = criarMedicalRecord(patientId);

            when(repository.findAllByPatientIdOrderByCreatedAtDesc(patientId)).thenReturn(List.of(record));

            List<PrescriptionResponseDTO> prescricoes = medicalRecordService.listarPrescricoesPaciente(patientId);

            assertThat(prescricoes).isNotEmpty();
            assertThat(prescricoes).hasSize(1);
            assertThat(prescricoes.get(0).medicationName()).isEqualTo("Losartana");
            assertThat(prescricoes.get(0).doctorName()).isEqualTo("Dr. João Silva");
            assertThat(prescricoes.get(0).isActive()).isTrue(); // Data de validade está no futuro no mock
        }
    }

    // ==========================================
    // 5. ADICIONAR ADENDO
    // ==========================================
    @DisplayName("Quando adicionar adendo a um prontuário")
    @Nested
    class AdicionarAdendo {

        @DisplayName("Dado um ID válido, deve concatenar o texto e salvar")
        @Test
        void sucesso() {
            String recordId = "mongo-id-123";
            MedicalRecord record = criarMedicalRecord(null);
            record.setPrivateNotes("Nota original.");

            when(repository.findById(recordId)).thenReturn(Optional.of(record));

            medicalRecordService.adicionarAdendo(recordId, "Nova evolução clínica.");

            verify(repository, times(1)).save(record);
            assertThat(record.getPrivateNotes()).contains("Nota original.");
            assertThat(record.getPrivateNotes()).contains("Nova evolução clínica.");
        }
    }
}