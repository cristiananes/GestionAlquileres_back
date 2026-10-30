package com.example.gestionalquilerback.service;
import com.example.gestionalquilerback.dto.UtilityBillRequest;
import com.example.gestionalquilerback.dto.UtilityBillRequest.*;
import java.math.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** Pure calculation, never writes cash or sends notifications. Dates are inclusive. */
public final class UtilitySplit {
 private UtilitySplit() {}
 public record Part(Recipient recipient, BigDecimal amount) {}
 public static List<Part> calculate(UtilityBillRequest r) {
  if(r.periodFrom()==null || r.periodTo()==null || r.periodTo().isBefore(r.periodFrom()) || ChronoUnit.DAYS.between(r.periodFrom(),r.periodTo())>1095)
   throw new IllegalArgumentException("El periodo debe ser válido y no superar tres años");
  if(r.amount()==null || r.amount().signum()<=0 || r.amount().scale()>2) throw new IllegalArgumentException("Indica un importe positivo con hasta dos decimales");
  if(r.method()==null || r.recipients()==null || r.recipients().isEmpty()) throw new IllegalArgumentException("Añade destinatarios y método");
  var people=new ArrayList<>(r.recipients());
  var weights=new ArrayList<BigDecimal>(); for(var ignored:people) weights.add(BigDecimal.ZERO);
  if(r.method()==Method.EQUAL) { for(int i=0;i<people.size();i++) weights.set(i,BigDecimal.ONE); }
  else if(r.method()==Method.PERCENT) {
   BigDecimal sum=BigDecimal.ZERO;
   for(int i=0;i<people.size();i++) { var n=people.get(i).percent(); if(n==null||n.signum()<0||n.compareTo(new BigDecimal("100"))>0) throw new IllegalArgumentException("Indica los porcentajes entre 0 y 100"); weights.set(i,n); sum=sum.add(n); }
   if(sum.compareTo(new BigDecimal("100"))!=0) throw new IllegalArgumentException("Los porcentajes deben sumar 100");
  } else {
   if(r.roomCount()<1||r.roomCount()>50||r.vacancy()==null) throw new IllegalArgumentException("Indica habitaciones y política de vacantes");
   if(people.stream().anyMatch(Recipient::owner)) throw new IllegalArgumentException("En el reparto por días el propietario se calcula automáticamente");
   for(var person:people) if(person.roomNumber()==null||person.roomNumber()<1||person.roomNumber()>r.roomCount()||person.from()==null||person.to()==null||person.to().isBefore(person.from())||person.from().isAfter(r.periodTo())||person.to().isBefore(r.periodFrom())) throw new IllegalArgumentException("Revisa habitación y estancia de cada destinatario");
   int owner=people.size(); people.add(new Recipient("Propietario · vacantes","",null,null,null,null,null,null,true)); weights.add(BigDecimal.ZERO);
   for(LocalDate day=r.periodFrom(); !day.isAfter(r.periodTo()); day=day.plusDays(1)) {
    var active=new ArrayList<Integer>(); var rooms=new HashSet<Integer>();
    for(int i=0;i<owner;i++) { var person=people.get(i); if(!day.isBefore(person.from())&&!day.isAfter(person.to())) { if(!rooms.add(person.roomNumber())) throw new IllegalArgumentException("Hay estancias solapadas en la habitación "+person.roomNumber()); active.add(i); } }
    if(active.isEmpty()) weights.set(owner,weights.get(owner).add(BigDecimal.ONE));
    else {
     var weight=BigDecimal.ONE.divide(BigDecimal.valueOf(r.vacancy()==Vacancy.OWNER?r.roomCount():active.size()),20,RoundingMode.HALF_EVEN);
     for(int i:active) weights.set(i,weights.get(i).add(weight));
     if(r.vacancy()==Vacancy.OWNER) weights.set(owner,weights.get(owner).add(BigDecimal.ONE.subtract(weight.multiply(BigDecimal.valueOf(active.size())))));
    }
   }
  }
  var total=weights.stream().reduce(BigDecimal.ZERO,BigDecimal::add); if(total.signum()==0) throw new IllegalArgumentException("El reparto no tiene destinatarios");
  long cents=r.amount().movePointRight(2).longValueExact(); long allocated=0;
  var result=new long[people.size()]; var fractions=new BigDecimal[people.size()];
  for(int i=0;i<people.size();i++) { var raw=weights.get(i).multiply(BigDecimal.valueOf(cents)).divide(total,16,RoundingMode.HALF_EVEN); result[i]=raw.longValue(); fractions[i]=raw.subtract(BigDecimal.valueOf(result[i])); allocated+=result[i]; }
  var order=new ArrayList<Integer>(); for(int i=0;i<people.size();i++) order.add(i);
  order.sort((a,b)-> {int cmp=fractions[b].compareTo(fractions[a]); return cmp!=0?cmp:Integer.compare(a,b);});
  for(long n=0;n<cents-allocated;n++) result[order.get((int)(n%order.size()))]++;
  var parts=new ArrayList<Part>(); for(int i=0;i<people.size();i++) parts.add(new Part(people.get(i),BigDecimal.valueOf(result[i],2)));
  return parts;
 }
}
