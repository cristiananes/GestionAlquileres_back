package com.example.gestionalquilerback.repository;
import com.example.gestionalquilerback.model.entity.Lease;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface LeaseRepository extends JpaRepository<Lease, Long> {
    List<Lease> findByPropertyId(Long propertyId);
    Optional<Lease> findByClientRequestIdAndUserId(String clientRequestId, Long userId);
}
