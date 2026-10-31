package com.example.gestionalquilerback.dto;
import java.time.LocalDate;import java.math.BigDecimal;import java.util.List;
public record SettlementResponse(Long id,Long propertyId,String propertyName,LocalDate from,LocalDate to,String ownerName,String ownerEmail,
 ManagementRequest.FeeMode feeMode,BigDecimal feeRate,BigDecimal taxRate,ManagementRequest.CollectionMode collectionMode,
 BigDecimal totalIncome,BigDecimal totalExpense,BigDecimal feeBase,BigDecimal feeNet,BigDecimal feeTax,BigDecimal feeTotal,BigDecimal netToOwner,
 String status,LocalDate paidDate,Long feeExpenseId,List<Line> lines,String reviewToken) {
 public record Line(String kind,Long movementId,LocalDate date,String description,BigDecimal amount) {}
}
