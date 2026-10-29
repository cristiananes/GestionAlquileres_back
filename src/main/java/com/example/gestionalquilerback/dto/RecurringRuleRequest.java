package com.example.gestionalquilerback.dto;
import com.example.gestionalquilerback.model.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.math.BigDecimal;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecurringRuleRequest {
    private Long propertyId;
    @NotNull private MovementKind kind;
    @NotNull private RecurrenceFrequency frequency;
    @NotNull @Positive @Digits(integer = 10, fraction = 2) private BigDecimal amount;
    @NotBlank @Size(max = 250) private String description;
    @NotNull private LocalDate startDate;
    private LocalDate endDate;
    private IncomeType incomeType;
    private PaymentMethod paymentMethod;
    private ExpenseCategory category;
    @NotBlank @Size(max = 80) private String clientRequestId;
}
