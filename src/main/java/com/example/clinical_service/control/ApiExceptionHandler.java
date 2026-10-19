package com.example.clinical_service.control;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> invalid(IllegalArgumentException error) {
        return ResponseEntity.badRequest().body(Map.of("message", error.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> validation(MethodArgumentNotValidException error) {
        return ResponseEntity.badRequest().body(Map.of("message", error.getBindingResult().getAllErrors().stream()
                .map(e -> e.getDefaultMessage()).collect(java.util.stream.Collectors.joining(" "))));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> status(ResponseStatusException error) {
        return ResponseEntity.status(error.getStatusCode()).body(Map.of("message", error.getReason() == null ? "Operação indisponível." : error.getReason()));
    }

    @ExceptionHandler(feign.FeignException.class)
    public ResponseEntity<?> upstream(feign.FeignException error) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("message", "Não foi possível consultar os serviços de cadastro ou agendamento."));
    }
}
