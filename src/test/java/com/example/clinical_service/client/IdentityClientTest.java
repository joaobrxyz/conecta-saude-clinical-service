package com.example.clinical_service.client;

import com.example.clinical_service.dto.DoctorInfoDTO;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@WireMockTest(httpPort = 8090) // Agora o servidor vai ligar corretamente antes dos métodos
@TestPropertySource(properties = {
        "identity.service.url=http://localhost:8090"
})
class IdentityClientTest {

    @Autowired
    private IdentityClient identityClient;

    @DisplayName("Deve retornar o DTO do médico quando o serviço responde 200 (OK)")
    @Test
    void getDoctorById_sucesso() {
        // Dado
        UUID doctorId = UUID.randomUUID();
        String jsonResponse = """
                {
                    "id": "%s",
                    "name": "Dr. João Silva",
                    "specialty": "Cardiologia"
                }
                """.formatted(doctorId);

        stubFor(get(urlEqualTo("/doctors/" + doctorId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonResponse)));

        // Quando
        DoctorInfoDTO atual = identityClient.getDoctorById(doctorId);

        // Então
        assertThat(atual).isNotNull();
        assertThat(atual.name()).isEqualTo("Dr. João Silva");
        assertThat(atual.specialty()).isEqualTo("Cardiologia");

        verify(1, getRequestedFor(urlEqualTo("/doctors/" + doctorId)));
    }

    @DisplayName("Deve lançar FeignException.NotFound quando o médico não for encontrado (404)")
    @Test
    void getDoctorById_falha() {
        // Dado
        UUID doctorId = UUID.randomUUID();

        stubFor(get(urlEqualTo("/doctors/" + doctorId))
                .willReturn(aResponse().withStatus(404)));

        // Quando / Então
        assertThatThrownBy(() -> identityClient.getDoctorById(doctorId))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @DisplayName("Deve retornar os dados do paciente quando o serviço responde 200 (OK)")
    @Test
    void getPatientById_sucesso() {
        // Dado
        UUID patientId = UUID.randomUUID();
        String jsonResponse = "{ \"id\": \"" + patientId + "\", \"name\": \"Maria Souza\" }";

        stubFor(get(urlEqualTo("/patients/" + patientId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonResponse)));

        // Quando
        Object atual = identityClient.getPatientById(patientId);

        // Então
        assertThat(atual).isNotNull();
        verify(1, getRequestedFor(urlEqualTo("/patients/" + patientId)));
    }

    @DisplayName("Deve lançar FeignException.NotFound quando o paciente não for encontrado (404)")
    @Test
    void getPatientById_falha() {
        // Dado
        UUID patientId = UUID.randomUUID();

        stubFor(get(urlEqualTo("/patients/" + patientId))
                .willReturn(aResponse().withStatus(404)));

        // Quando / Então
        assertThatThrownBy(() -> identityClient.getPatientById(patientId))
                .isInstanceOf(FeignException.NotFound.class);
    }
}