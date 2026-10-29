package com.example.gestionalquilerback.repository;

import com.example.gestionalquilerback.model.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IncomeRepository extends JpaRepository<Income, Long> {

    List<Income> findByPropertyId(Long propertyId);

    List<Income> findByIncomeDateBetween(LocalDate start, LocalDate end);

    List<Income> findByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.archived = false AND i.user.id = :userId AND i.incomeDate BETWEEN :start AND :end")
    BigDecimal sumByIncomeDateBetween(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.archived = false AND i.user.id = :userId")
    BigDecimal sumAll(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.archived = false AND i.incomeDate BETWEEN :start AND :end")
    BigDecimal sumByIncomeDateBetweenGlobal(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.archived = false")
    BigDecimal sumAllGlobal();
    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i " +
            "WHERE i.archived = false AND (:userId IS NULL OR i.user.id = :userId) " +
            "AND (:from IS NULL OR i.incomeDate >= :from) " +
            "AND (:to IS NULL OR i.incomeDate <= :to)")
    BigDecimal sumForPeriod(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);
    java.util.Optional<Income> findByClientRequestIdAndUserId(String clientRequestId, Long userId);
    List<Income> findByOccurrenceIdAndArchivedFalse(Long occurrenceId);
}
