package com.example.gestionalquilerback.repository;
import com.example.gestionalquilerback.model.entity.Lease;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface LeaseRepository extends JpaRepository<Lease, Long> {
 @org.springframework.data.jpa.repository.Query(value="select * from lease where property_id=:propertyId for update",nativeQuery=true)
 java.util.List<Lease> lockPropertyLeases(@org.springframework.data.repository.query.Param("propertyId") Long propertyId);

    @org.springframework.data.jpa.repository.Query(value="select * from lease where id=:id for update",nativeQuery=true)
    Optional<Lease> lockLease(@org.springframework.data.repository.query.Param("id") Long id);
    List<Lease> findByPropertyId(Long propertyId);
    Optional<Lease> findByClientRequestIdAndUserId(String clientRequestId, Long userId);
}
