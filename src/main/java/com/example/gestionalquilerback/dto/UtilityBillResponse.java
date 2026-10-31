package com.example.gestionalquilerback.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.example.gestionalquilerback.dto.UtilityBillRequest.*;
public record UtilityBillResponse(Long id, Long propertyId,String propertyName,String supply,String provider,String reference,
 BigDecimal amount,LocalDate periodFrom,LocalDate periodTo,LocalDate dueDate,Method method,int roomCount,Vacancy vacancy,
 Long documentId,String filename,boolean archived,Long expenseId,boolean expensePaid, List<Share> shares) {
 public record Payment(Long incomeId,BigDecimal amount,LocalDate date) {}
 public record Share(Long id,String name,String room,String email,String phone,Integer roomNumber,LocalDate from,LocalDate to,BigDecimal percent,
  boolean owner,BigDecimal amount,BigDecimal paid,BigDecimal pending,String status,List<Payment> payments) {}
}
