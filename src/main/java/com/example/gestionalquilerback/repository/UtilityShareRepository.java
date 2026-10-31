package com.example.gestionalquilerback.repository;
import com.example.gestionalquilerback.model.entity.UtilityShare;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface UtilityShareRepository extends JpaRepository<UtilityShare,Long> {
 List<UtilityShare> findByBillIdOrderByIdAsc(Long billId);
}
