package com.example.gestionalquilerback.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public record UtilityPaymentRequest(@NotNull @DecimalMin("0.01") @DecimalMax("9999999.99") @Digits(integer=7,fraction=2) BigDecimal amount,
 @NotNull @PastOrPresent LocalDate date, @NotBlank @Size(max=80) String clientRequestId, Long movementId, com.example.gestionalquilerback.model.enums.PaymentMethod paymentMethod) {}
