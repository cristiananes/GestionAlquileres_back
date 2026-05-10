package com.example.gestionalquilerback.dto;

import com.example.gestionalquilerback.model.enums.IncomeType;
import com.example.gestionalquilerback.model.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IncomeRequest {
    private Long propertyId;

    @NotNull @Positive
    private BigDecimal amount;

    @NotBlank
    private String description;

    @NotNull
    private LocalDate incomeDate;

    @NotNull
    private IncomeType incomeType;

    private PaymentMethod paymentMethod;
}
