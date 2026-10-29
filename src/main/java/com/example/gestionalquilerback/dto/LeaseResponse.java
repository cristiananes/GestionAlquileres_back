package com.example.gestionalquilerback.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
public record LeaseResponse(Long id, Long propertyId, String tenantName, String tenantEmail, String tenantPhone, LocalDate startDate, LocalDate endDate,
        BigDecimal monthlyRent, BigDecimal depositAmount, int paymentDay, Long recurringRuleId, boolean archived) {}
