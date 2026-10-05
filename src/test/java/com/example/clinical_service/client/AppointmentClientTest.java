package com.example.clinical_service.client;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@WireMockTest(httpPort = 8089) // Sobe um servidor falso na porta 8089
@TestPropertySource(properties = {
        "appointment.service.url=http://localhost:8089" // Força o Feign a apontar para o servidor falso
})
class AppointmentClientTest {

    @Autowired
    private AppointmentClient appointmentClient;

    @DisplayName("Quando buscar um agendamento por ID via Feign")
    @Nested
    class GetAppointmentById {

        @DisplayName("Deve retornar os dados quando o serviço externo responde 200 (OK)")
        @Test
        void sucesso() {
            // Dado
            UUID appointmentId = UUID.randomUUID();
            String jsonResponse = "{ \"id\": \"" + appointmentId + "\", \"status\": \"CONFIRMADO\" }";

            // Ensina o servidor falso (WireMock) a responder HTTP 200 com o JSON
            stubFor(get(urlEqualTo("/appointments/" + appointmentId))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(jsonResponse)));

            // Quando
            Object atual = appointmentClient.getAppointmentById(appointmentId);

            // Então
            assertThat(atual).isNotNull();
            // Verifica se a chamada HTTP realmente aconteceu na URL certa
            verify(1, getRequestedFor(urlEqualTo("/appointments/" + appointmentId)));
        }

        @DisplayName("Deve lançar FeignException.NotFound quando o serviço externo responde 404")
        @Test
        void falha() {
            // Dado
            UUID appointmentId = UUID.randomUUID();

            // Ensina o servidor falso a responder um erro 404
            stubFor(get(urlEqualTo("/appointments/" + appointmentId))
                    .willReturn(aResponse().withStatus(404)));

            // Quando / Então
            assertThatThrownBy(() -> appointmentClient.getAppointmentById(appointmentId))
                    .isInstanceOf(FeignException.NotFound.class);
        }
    }
}