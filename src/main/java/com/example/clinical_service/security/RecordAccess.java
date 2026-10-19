package com.example.clinical_service.security;

import com.example.clinical_service.model.MedicalRecord;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class RecordAccess {
    private RecordAccess() {}

    public static MedicalRecord visible(MedicalRecord record) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean doctor = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MEDICO"));
        if (!doctor && !record.getPatientId().toString().equals(auth.getName())) {
            throw new AccessDeniedException("Você não tem acesso a este prontuário.");
        }
        MedicalRecord result = new MedicalRecord();
        BeanUtils.copyProperties(record, result);
        if (!doctor || !record.getDoctorId().toString().equals(auth.getName())) result.setPrivateNotes(null);
        return result;
    }

    public static void requireAuthor(MedicalRecord record) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!record.getDoctorId().toString().equals(auth.getName())) {
            throw new AccessDeniedException("Somente o médico responsável pode adicionar adendos.");
        }
    }
}
