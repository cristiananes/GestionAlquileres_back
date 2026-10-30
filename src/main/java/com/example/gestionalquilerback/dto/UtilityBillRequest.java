package com.example.gestionalquilerback.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
public record UtilityBillRequest(
 @NotNull Long propertyId, @NotBlank @Size(max=80) String clientRequestId,
 @NotBlank @Size(max=30) String supply, @NotBlank @Size(max=120) String provider,
 @NotBlank @Size(max=100) String reference,
 @NotNull @DecimalMin("0.01") @DecimalMax("9999999.99") @Digits(integer=7,fraction=2) BigDecimal amount,
 @NotNull LocalDate periodFrom, @NotNull LocalDate periodTo, @NotNull LocalDate dueDate,
 @NotNull Method method, @Min(1) @Max(50) int roomCount, @NotNull Vacancy vacancy,
 @NotEmpty @Size(max=100) List<@Valid Recipient> recipients) {
 public enum Method { EQUAL, PERCENT, DAYS }
 public enum Vacancy { OWNER, OCCUPANTS }
 public record Recipient(@NotBlank @Size(max=120) String name, @Size(max=120) String room,
  @Email @Size(max=180) String email, @Size(max=40) String phone,
  @Min(1) @Max(50) Integer roomNumber, LocalDate from, LocalDate to,
  @DecimalMin("0") @DecimalMax("100") @Digits(integer=3,fraction=4) BigDecimal percent, boolean owner) {}
}
