package com.example.gestionalquilerback.repository;
import com.example.gestionalquilerback.model.entity.OwnerSettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface OwnerSettlementRepository extends JpaRepository<OwnerSettlement,Long> {
 List<OwnerSettlement> findByPropertyIdOrderByIdDesc(Long propertyId);
 Optional<OwnerSettlement> findByRequestKey(String key);
}
