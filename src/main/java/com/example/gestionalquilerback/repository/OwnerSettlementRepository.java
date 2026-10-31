package com.example.gestionalquilerback.repository;
import com.example.gestionalquilerback.model.entity.OwnerSettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface OwnerSettlementRepository extends JpaRepository<OwnerSettlement,Long> {
 @org.springframework.data.jpa.repository.Query(value="select * from owner_settlement where property_id=:propertyId for update",nativeQuery=true)
 java.util.List<OwnerSettlement> lockByProperty(@org.springframework.data.repository.query.Param("propertyId") Long propertyId);

 List<OwnerSettlement> findByPropertyIdOrderByIdDesc(Long propertyId);
 Optional<OwnerSettlement> findByRequestKey(String key);
}
