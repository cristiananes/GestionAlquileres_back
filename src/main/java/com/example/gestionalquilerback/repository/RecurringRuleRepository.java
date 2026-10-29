package com.example.gestionalquilerback.repository;
import com.example.gestionalquilerback.model.entity.RecurringRule;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface RecurringRuleRepository extends JpaRepository<RecurringRule, Long> {
    List<RecurringRule> findByUserId(Long userId);
    List<RecurringRule> findByPropertyId(Long propertyId);
    Optional<RecurringRule> findByClientRequestIdAndUserId(String clientRequestId, Long userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select r from RecurringRule r where r.id = :id")
    Optional<RecurringRule> lockById(@org.springframework.data.repository.query.Param("id") Long id);
}
