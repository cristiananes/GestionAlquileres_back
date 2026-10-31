package com.example.gestionalquilerback.repository;
import com.example.gestionalquilerback.model.entity.SettlementLine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface SettlementLineRepository extends JpaRepository<SettlementLine,Long> {
 List<SettlementLine> findBySettlementIdOrderByIdAsc(Long settlementId);
 @org.springframework.data.jpa.repository.Query(value="select * from settlement_line where active_key=:key for update",nativeQuery=true)
 Optional<SettlementLine> lockActive(@org.springframework.data.repository.query.Param("key") String activeKey);
 boolean existsByActiveKey(String activeKey);
}
