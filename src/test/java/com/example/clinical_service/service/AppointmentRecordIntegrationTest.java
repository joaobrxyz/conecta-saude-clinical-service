package com.example.clinical_service.service;

import com.example.clinical_service.client.AppointmentClient;
import com.example.clinical_service.client.IdentityClient;
import com.example.clinical_service.dto.*;
import com.example.clinical_service.model.MedicalRecord;
import com.example.clinical_service.repository.MedicalRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class AppointmentRecordIntegrationTest {
    @Mock MedicalRecordRepository repository;
    @Mock IdentityClient identityClient;
    @Mock AppointmentClient appointmentClient;
    @InjectMocks MedicalRecordService service;
    private final UUID doctor = UUID.randomUUID();
    private final UUID patient = UUID.randomUUID();
    private final UUID appointment = UUID.randomUUID();
    private MedicalRecordRequestDTO request() { return new MedicalRecordRequestDTO(patient, appointment, "Consulta", "Evolução", null, null, List.of()); }
    private void appointment(UUID doctorId, UUID patientId, String status) {
        when(identityClient.getDoctorById(doctor)).thenReturn(new DoctorInfoDTO(doctor, "Médico", "Clínica"));
        when(appointmentClient.getAppointmentById(appointment)).thenReturn(new AppointmentInfoDTO(appointment, doctorId, patientId, status));
    }
    @Test void rejectsWrongPatient() {
        appointment(doctor, UUID.randomUUID(), "EM_ATENDIMENTO"); assertThatThrownBy(() -> service.criarRegisto(doctor, request())).isInstanceOf(IllegalArgumentException.class); verify(repository, never()).save(any());
    }
    @Test void rejectsWrongDoctor() {
        appointment(UUID.randomUUID(), patient, "EM_ATENDIMENTO"); assertThatThrownBy(() -> service.criarRegisto(doctor, request())).isInstanceOf(IllegalArgumentException.class); verify(repository, never()).save(any());
    }
    @Test void requiresStartedAppointment() {
        appointment(doctor, patient, "AGENDADA"); assertThatThrownBy(() -> service.criarRegisto(doctor, request())).isInstanceOf(IllegalArgumentException.class); verify(repository, never()).save(any());
    }
    @Test void savesCorrectAppointmentAndDoctorDetails() {
        appointment(doctor, patient, "EM_ATENDIMENTO"); when(repository.save(any())).thenAnswer(invocation -> { MedicalRecord record = invocation.getArgument(0); record.setId("record-1"); return record; });
        var result = service.criarRegisto(doctor, request()); assertThat(result.id()).isEqualTo("record-1"); assertThat(result.doctorName()).isEqualTo("Médico");
    }
}
