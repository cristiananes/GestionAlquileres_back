package com.example.gestionalquilerback.dto;
import java.time.LocalDateTime;
public record DocumentResponse(Long id, Long propertyId, Long incomeId, Long expenseId, String filename, String contentType, long size, boolean archived, LocalDateTime createdAt) {}
