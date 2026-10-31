package com.example.gestionalquilerback.dto;
import java.time.LocalDate;import jakarta.validation.constraints.*;
public record SettlementPaidRequest(@NotNull @PastOrPresent LocalDate date) {}
