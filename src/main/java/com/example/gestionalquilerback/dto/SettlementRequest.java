package com.example.gestionalquilerback.dto;
import jakarta.validation.constraints.*;import java.time.LocalDate;import java.util.List;
public record SettlementRequest(@NotNull Long propertyId,@NotNull LocalDate from,@NotNull LocalDate to,
 @NotNull @Size(max=1000) List<@NotNull Long> incomeIds,@NotNull @Size(max=1000) List<@NotNull Long> expenseIds,
 @NotBlank @Size(max=80) String clientRequestId,@Size(max=64) String previewToken) {}
