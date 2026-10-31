package com.example.gestionalquilerback.dto;
import jakarta.validation.constraints.*;
public record RoomRequest(@NotBlank @Size(max=100) String name,@Min(1) @Max(50) int number) {}
