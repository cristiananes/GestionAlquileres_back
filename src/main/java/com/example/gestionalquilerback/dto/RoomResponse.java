package com.example.gestionalquilerback.dto;
import java.time.LocalDate;
import java.util.List;
public record RoomResponse(Long id,Long propertyId,String name,int number,boolean archived,LocalDate on,String status,List<Stay> stays) {
 public record Stay(Long leaseId,String tenantName,LocalDate from,LocalDate to,boolean entireProperty) {}
}
