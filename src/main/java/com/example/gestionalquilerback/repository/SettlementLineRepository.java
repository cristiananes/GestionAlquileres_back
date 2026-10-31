package com.example.gestionalquilerback.repository;
import com.example.gestionalquilerback.model.entity.SettlementLine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface SettlementLineRepository extends JpaRepository<SettlementLine,Long> {
 List<SettlementLine> findBySettlementIdOrderByIdAsc(Long settlementId);
 boolean existsByActiveKey(String activeKey);
}
