package com.example.gestionalquilerback.dto;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentRequest {
    @NotNull private LocalDate dueDate;
    @NotNull private LocalDate paymentDate;
    @NotNull @Positive @Digits(integer = 10, fraction = 2) private BigDecimal amount;
    @NotBlank @Size(max = 80) private String clientRequestId;
}
