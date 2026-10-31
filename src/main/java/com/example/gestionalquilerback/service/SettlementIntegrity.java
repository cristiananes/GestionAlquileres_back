package com.example.gestionalquilerback.service;
import com.example.gestionalquilerback.repository.SettlementLineRepository;
import lombok.RequiredArgsConstructor;import org.springframework.stereotype.Component;
@Component @RequiredArgsConstructor
public class SettlementIntegrity {
 private final SettlementLineRepository lines;
 public void ensureFree(String kind,Long id) {if(lines.lockActive(kind+":"+id).isPresent()) throw new IllegalArgumentException("El movimiento pertenece a una liquidación confirmada. Anula esa liquidación antes de modificarlo o vincularlo a un recibo");}
}
