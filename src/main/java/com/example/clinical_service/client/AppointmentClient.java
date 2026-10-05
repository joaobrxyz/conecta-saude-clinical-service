package com.example.clinical_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "appointment-service", url = "${appointment.service.url}")
public interface AppointmentClient {

    @GetMapping("/appointments/{id}")
    Object getAppointmentById(@PathVariable("id") UUID id);
}