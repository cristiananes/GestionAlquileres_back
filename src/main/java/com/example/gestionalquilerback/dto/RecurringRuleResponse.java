package com.example.gestionalquilerback.dto;
import com.example.gestionalquilerback.model.enums.*;
import java.math.BigDecimal;
import java.time.*;
public record RecurringRuleResponse(Long id, Long propertyId, String propertyName, MovementKind kind,
        RecurrenceFrequency frequency, BigDecimal amount, String description, LocalDate startDate, LocalDate endDate,
        IncomeType incomeType, PaymentMethod paymentMethod, ExpenseCategory category, boolean archived) {}
