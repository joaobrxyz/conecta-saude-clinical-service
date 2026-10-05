package com.example.clinical_service.control;

import com.example.clinical_service.dto.MedicalRecordRequestDTO;
import com.example.clinical_service.dto.MedicalRecordSummaryDTO;
import com.example.clinical_service.dto.PrescriptionDTO;
import com.example.clinical_service.dto.PrescriptionResponseDTO;
import com.example.clinical_service.dto.VitalsDTO;
import com.example.clinical_service.model.MedicalRecord;
import com.example.clinical_service.model.Vitals;
import com.example.clinical_service.service.MedicalRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalRecordControllerTest {

    @InjectMocks
    MedicalRecordController controller;

    @Mock
    MedicalRecordService service;

    @Mock
    HttpServletRequest servletRequest; // Essencial para simular o ID vindo do token JWT

    // --- MÉTODOS AUXILIARES ---
    private MedicalRecordRequestDTO criarRequestDTO() {
        return new MedicalRecordRequestDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Consulta de Rotina",
                "Paciente apresenta melhoras.",
                "Nota privada",
                new VitalsDTO(75.0, 1.80, "120/80"),
                List.of(new PrescriptionDTO("Losartana", "50mg", "1x ao dia", LocalDate.now().plusDays(30)))
        );
    }

    private MedicalRecordSummaryDTO criarSummaryDTO() {
        return new MedicalRecordSummaryDTO(
                "mongo-id-123",
                LocalDateTime.now(),
                "Consulta de Rotina",
                "Dr. João Silva",
                "Cardiologia",
                "Paciente apresenta melhoras."
        );
    }

    // ==========================================
    // 1. GESTÃO CENTRAL DE PRONTUÁRIOS
    // ==========================================
    @DisplayName("Quando criar um registo clínico")
    @Nested
    class CriarRegisto {

        @DisplayName("Deve extrair ID do request, chamar o service e retornar HTTP 201 (CREATED)")
        @Test
        void sucesso() {
            // Dado
            UUID doctorId = UUID.randomUUID();
            var requestDTO = criarRequestDTO();
            var summaryDTO = criarSummaryDTO();

            // Simula o filtro JWT injetando o ID do médico no request
            when(servletRequest.getAttribute("userId")).thenReturn(doctorId.toString());
            when(service.criarRegisto(doctorId, requestDTO)).thenReturn(summaryDTO);

            // Quando
            ResponseEntity<MedicalRecordSummaryDTO> resposta = controller.criarRegisto(requestDTO, servletRequest);

            // Então
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(resposta.getBody()).isNotNull();
            assertThat(resposta.getBody().doctorName()).isEqualTo("Dr. João Silva");

            verify(service, times(1)).criarRegisto(doctorId, requestDTO);
        }
    }

    @DisplayName("Quando buscar prontuário por ID")
    @Nested
    class BuscarProntuarioDetalhado {

        @DisplayName("Deve chamar o service e retornar HTTP 200 (OK)")
        @Test
        void sucesso() {
            // Dado
            String recordId = "mongo-id-123";
            MedicalRecord mockRecord = new MedicalRecord();
            mockRecord.setId(recordId);

            when(service.buscarPorId(recordId)).thenReturn(mockRecord);

            // Quando
            ResponseEntity<MedicalRecord> resposta = controller.buscarProntuarioDetalhado(recordId);

            // Então
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isNotNull();
            assertThat(resposta.getBody().getId()).isEqualTo(recordId);
        }
    }

    @DisplayName("Quando listar histórico do paciente")
    @Nested
    class ListarHistoricoPaciente {

        @DisplayName("Deve chamar o service e retornar a lista com HTTP 200 (OK)")
        @Test
        void sucesso() {
            UUID patientId = UUID.randomUUID();
            when(service.listarHistoricoPaciente(patientId)).thenReturn(List.of(criarSummaryDTO()));

            ResponseEntity<List<MedicalRecordSummaryDTO>> resposta = controller.listarHistoricoPaciente(patientId);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).hasSize(1);
        }
    }

    @DisplayName("Quando adicionar um adendo")
    @Nested
    class AdicionarAdendo {

        @DisplayName("Deve chamar o service e retornar HTTP 204 (NO CONTENT)")
        @Test
        void sucesso() {
            String recordId = "mongo-id-123";
            String texto = "Nova anotação médica";

            doNothing().when(service).adicionarAdendo(recordId, texto);

            ResponseEntity<Void> resposta = controller.adicionarAdendo(recordId, texto);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(service, times(1)).adicionarAdendo(recordId, texto);
        }
    }

    // ==========================================
    // 2. SINAIS VITAIS & PRESCRIÇÕES
    // ==========================================
    @DisplayName("Quando buscar os últimos sinais vitais")
    @Nested
    class BuscarUltimosSinaisVitais {

        @DisplayName("Deve retornar os sinais vitais com HTTP 200 (OK)")
        @Test
        void sucesso() {
            UUID patientId = UUID.randomUUID();
            Vitals vitalsMock = new Vitals(75.0, 1.80, "120/80");

            when(service.buscarUltimosSinaisVitais(patientId)).thenReturn(vitalsMock);

            ResponseEntity<Vitals> resposta = controller.buscarUltimosSinaisVitais(patientId);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isNotNull();
            assertThat(resposta.getBody().getBloodPressure()).isEqualTo("120/80");
        }
    }

    @DisplayName("Quando solicitar renovação de receita")
    @Nested
    class SolicitarRenovacaoReceita {

        @DisplayName("Deve chamar o service e retornar HTTP 202 (ACCEPTED)")
        @Test
        void sucesso() {
            UUID prescriptionId = UUID.randomUUID();

            doNothing().when(service).solicitarRenovacao(prescriptionId);

            ResponseEntity<Void> resposta = controller.solicitarRenovacaoReceita(prescriptionId);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
            verify(service, times(1)).solicitarRenovacao(prescriptionId);
        }
    }

    // ==========================================
    // 3. EXPORTAÇÃO (PDF)
    // ==========================================
    @DisplayName("Quando exportar histórico para PDF")
    @Nested
    class ExportarHistoricoCompletoPDF {

        @DisplayName("Deve retornar os bytes do arquivo e cabeçalhos apropriados")
        @Test
        void sucesso() {
            UUID patientId = UUID.randomUUID();
            byte[] mockPdfBytes = "Mock PDF Content".getBytes();

            when(service.gerarPdfHistorico(patientId)).thenReturn(mockPdfBytes);

            ResponseEntity<byte[]> resposta = controller.exportarHistoricoCompletoPDF(patientId);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isEqualTo(mockPdfBytes);

            // Verifica se o cabeçalho para forçar o download foi adicionado corretamente
            assertThat(resposta.getHeaders().get("Content-Disposition").get(0))
                    .isEqualTo("attachment; filename=historico.pdf");
        }
    }
}