package com.example.gestionalquilerback.repository;
import com.example.gestionalquilerback.model.entity.UtilityBill;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface UtilityBillRepository extends JpaRepository<UtilityBill,Long> {
 List<UtilityBill> findByUserIdOrderByIdDesc(Long userId);
 Optional<UtilityBill> findByClientRequestIdAndUserId(String key,Long userId);
 Optional<UtilityBill> findByFingerprint(String fingerprint);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select b from UtilityBill b where b.id=:id") Optional<UtilityBill> lockById(@Param("id") Long id);
}
