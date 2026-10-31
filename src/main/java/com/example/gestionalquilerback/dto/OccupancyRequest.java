package com.example.gestionalquilerback.dto;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import com.example.gestionalquilerback.model.enums.RentalScope;
public record OccupancyRequest(@NotNull RentalScope rentalScope, Long roomId, @NotNull LocalDate from, LocalDate to) {}
