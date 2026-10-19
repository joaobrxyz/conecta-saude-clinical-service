package com.example.clinical_service.security;

import com.example.clinical_service.model.MedicalRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class RecordAccessTest {
    private final UUID patient = UUID.randomUUID();
    private final UUID doctor = UUID.randomUUID();
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    private void login(UUID id, String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(id.toString(), null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }
    private MedicalRecord record() {
        var record = new MedicalRecord(); record.setPatientId(patient); record.setDoctorId(doctor); record.setPrivateNotes("Confidential"); return record;
    }
    @Test void patientCannotReadAnotherPatient() {
        login(UUID.randomUUID(), "PACIENTE"); assertThatThrownBy(() -> RecordAccess.visible(record())).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }
    @Test void patientCannotReceivePrivateNotesAndOriginalIsPreserved() {
        login(patient, "PACIENTE"); var record = record(); assertThat(RecordAccess.visible(record).getPrivateNotes()).isNull(); assertThat(record.getPrivateNotes()).isEqualTo("Confidential");
    }
    @Test void authorCanReadNotes() {
        login(doctor, "MEDICO"); assertThat(RecordAccess.visible(record()).getPrivateNotes()).isEqualTo("Confidential");
    }
    @Test void anotherDoctorCannotReadOrAppendPrivateNotes() {
        login(UUID.randomUUID(), "MEDICO"); assertThat(RecordAccess.visible(record()).getPrivateNotes()).isNull(); assertThatThrownBy(() -> RecordAccess.requireAuthor(record())).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }
}
