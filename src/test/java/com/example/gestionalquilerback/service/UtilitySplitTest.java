package com.example.gestionalquilerback.service;
import com.example.gestionalquilerback.dto.UtilityBillRequest;
import com.example.gestionalquilerback.dto.UtilityBillRequest.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class UtilitySplitTest {
 static final LocalDate FROM=LocalDate.of(2026,1,1), TO=LocalDate.of(2026,1,31);
 static Recipient person(String name,Integer room,LocalDate from,LocalDate to,String percent) {return new Recipient(name,"",null,null,room,from,to,percent==null?null:new BigDecimal(percent),false);}
 static UtilityBillRequest request(String amount,Method method,Vacancy vacancy,List<Recipient> people) {return new UtilityBillRequest(1L,"key","Luz","Proveedor","Factura",new BigDecimal(amount),FROM,TO,TO,method,2,vacancy,people);}
 @Test void penniesAreConservedWithDeterministicRemainder() {
  var parts=UtilitySplit.calculate(request("10.00",Method.EQUAL,Vacancy.OWNER,List.of(person("A",null,null,null,null),person("B",null,null,null,null),person("C",null,null,null,null))));
  assertEquals(new BigDecimal("3.34"),parts.getFirst().amount()); assertEquals(new BigDecimal("10.00"),parts.stream().map(UtilitySplit.Part::amount).reduce(BigDecimal.ZERO,BigDecimal::add));
 }
 @Test void customPercentagesMustTotalOneHundred() {
  assertThrows(IllegalArgumentException.class,()->UtilitySplit.calculate(request("10.00",Method.PERCENT,Vacancy.OWNER,List.of(person("A",null,null,null,"90")))));
  var p=UtilitySplit.calculate(request("10.00",Method.PERCENT,Vacancy.OWNER,List.of(person("A",null,null,null,"25"),person("B",null,null,null,"75"))));
  assertEquals(new BigDecimal("2.50"),p.getFirst().amount());
 }
 @Test void changeOfTenantAndVacanciesBelongToOwner() {
  var p=UtilitySplit.calculate(request("31.00",Method.DAYS,Vacancy.OWNER,List.of(person("A",1,FROM,FROM.plusDays(14),null),person("B",1,FROM.plusDays(15),TO,null))));
  assertEquals(List.of(new BigDecimal("7.50"),new BigDecimal("8.00"),new BigDecimal("15.50")),p.stream().map(UtilitySplit.Part::amount).toList());
 }
 @Test void vacantRoomCanBeRedistributedAndEmptyDaysStillBelongToOwner() {
  var p=UtilitySplit.calculate(request("31.00",Method.DAYS,Vacancy.OCCUPANTS,List.of(person("A",1,FROM,FROM.plusDays(9),null))));
  assertEquals(new BigDecimal("10.00"),p.getFirst().amount()); assertEquals(new BigDecimal("21.00"),p.getLast().amount());
 }
 @Test void overlappingOccupantsAndInvalidPeriodsAreRejected() {
  assertThrows(IllegalArgumentException.class,()->UtilitySplit.calculate(request("31.00",Method.DAYS,Vacancy.OWNER,List.of(person("A",1,FROM,TO,null),person("B",1,FROM,TO,null)))));
  assertThrows(IllegalArgumentException.class,()->UtilitySplit.calculate(request("0.00",Method.EQUAL,Vacancy.OWNER,List.of(person("A",null,null,null,null)))));
 }
}
