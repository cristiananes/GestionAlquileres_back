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
    @jakarta.validation.constraints.Size(max = 80)
    private String clientRequestId;
    private Long propertyId;

    @NotNull @Positive @jakarta.validation.constraints.Digits(integer = 8, fraction = 2)
    private BigDecimal amount;

    @NotBlank
    private String description;

    @NotNull
    private LocalDate incomeDate;

    @NotNull
    private IncomeType incomeType;

    private PaymentMethod paymentMethod;
}
