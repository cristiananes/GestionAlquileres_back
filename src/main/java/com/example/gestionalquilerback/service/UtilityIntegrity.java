package com.example.gestionalquilerback.service;
import com.example.gestionalquilerback.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
@Component @RequiredArgsConstructor
public class UtilityIntegrity {
 private final UtilityShareRepository shares;
 private final UtilityBillRepository bills;
 private final IncomeRepository incomes;
 public void lock(Long shareId,Long excludeIncomeId,BigDecimal restoring) {
  if(shareId==null) return;
  var s=shares.findById(shareId).orElseThrow(); var b=bills.lockById(s.getBill().getId()).orElseThrow();
  if(restoring!=null) {
   if(b.isArchived()) throw new IllegalArgumentException("Restaura el recibo antes de restaurar el reembolso");
   var paid=incomes.findByUtilityShareId(shareId).stream().filter(i->!i.isArchived()&&!i.getId().equals(excludeIncomeId)).map(i->i.getAmount()).reduce(BigDecimal.ZERO,BigDecimal::add);
   if(paid.add(restoring).compareTo(s.getAmount())>0) throw new IllegalArgumentException("El cobro supera el importe pendiente del suministro");
  }
 }
}
