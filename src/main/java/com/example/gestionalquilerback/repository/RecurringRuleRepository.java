package com.example.gestionalquilerback.repository;
import com.example.gestionalquilerback.model.entity.RecurringRule;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface RecurringRuleRepository extends JpaRepository<RecurringRule, Long> {
    List<RecurringRule> findByUserId(Long userId);
    List<RecurringRule> findByPropertyId(Long propertyId);
    Optional<RecurringRule> findByClientRequestIdAndUserId(String clientRequestId, Long userId);
    @org.springframework.data.jpa.repository.Query(value="select * from recurring_rule where id=:id for update",nativeQuery=true)
    Optional<RecurringRule> lockById(@org.springframework.data.repository.query.Param("id") Long id);
}
