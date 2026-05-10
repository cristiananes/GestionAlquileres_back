package com.example.gestionalquilerback.dto;

import com.example.gestionalquilerback.model.enums.IncomeType;
import com.example.gestionalquilerback.model.enums.PaymentMethod;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IncomeResponse {
    private Long id;
    private Long propertyId;
    private String propertyName;
    private BigDecimal amount;
    private String description;
    private LocalDate incomeDate;
    private IncomeType incomeType;
    private PaymentMethod paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
