package com.example.gestionalquilerback.dto;
import com.example.gestionalquilerback.model.enums.*;
import java.time.LocalDate;
import java.math.BigDecimal;
public record OccurrenceResponse(Long ruleId, Long propertyId, String propertyName, MovementKind kind,
        String description, LocalDate dueDate, BigDecimal amount, BigDecimal paidAmount, BigDecimal remaining,
        String status) {}
