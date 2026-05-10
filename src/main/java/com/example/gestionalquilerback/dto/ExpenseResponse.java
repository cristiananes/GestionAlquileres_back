package com.example.gestionalquilerback.dto;

import com.example.gestionalquilerback.model.enums.ExpenseCategory;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExpenseResponse {
    private Long id;
    private Long propertyId;
    private String propertyName;
    private BigDecimal amount;
    private String description;
    private LocalDate expenseDate;
    private ExpenseCategory category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
