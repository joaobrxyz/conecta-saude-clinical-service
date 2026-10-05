package com.example.clinical_service.client;

import com.example.clinical_service.dto.DoctorInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "identity-service", url = "${identity.service.url}")
public interface IdentityClient {

    @GetMapping("/doctors/{id}")
    DoctorInfoDTO getDoctorById(@PathVariable("id") UUID id); // <-- Alterado aqui

    @GetMapping("/patients/{id}")
    Object getPatientById(@PathVariable("id") UUID id);
}