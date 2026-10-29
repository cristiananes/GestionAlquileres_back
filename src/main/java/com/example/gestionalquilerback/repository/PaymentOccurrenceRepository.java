package com.example.gestionalquilerback.repository;
import com.example.gestionalquilerback.model.entity.PaymentOccurrence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import java.time.LocalDate;
public interface PaymentOccurrenceRepository extends JpaRepository<PaymentOccurrence, Long> {
    Optional<PaymentOccurrence> findByRuleIdAndDueDate(Long ruleId, LocalDate dueDate);
    List<PaymentOccurrence> findByRuleId(Long ruleId);
}
