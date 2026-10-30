package com.example.gestionalquilerback.repository;

import com.example.gestionalquilerback.model.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select m from Expense m where m.id=:id")
    java.util.Optional<Expense> lockMovement(@org.springframework.data.repository.query.Param("id") Long id);

    java.util.Optional<Expense> findByUtilityBillId(Long utilityBillId);


    List<Expense> findByPropertyId(Long propertyId);

    List<Expense> findByExpenseDateBetween(LocalDate start, LocalDate end);

    List<Expense> findByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.archived = false AND e.user.id = :userId AND e.expenseDate BETWEEN :start AND :end")
    BigDecimal sumByExpenseDateBetween(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.archived = false AND e.user.id = :userId")
    BigDecimal sumAll(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.archived = false AND e.expenseDate BETWEEN :start AND :end")
    BigDecimal sumByExpenseDateBetweenGlobal(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.archived = false")
    BigDecimal sumAllGlobal();
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.archived = false AND (:userId IS NULL OR e.user.id = :userId) " +
            "AND (:from IS NULL OR e.expenseDate >= :from) " +
            "AND (:to IS NULL OR e.expenseDate <= :to)")
    BigDecimal sumForPeriod(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);
    java.util.Optional<Expense> findByClientRequestIdAndUserId(String clientRequestId, Long userId);
    List<Expense> findByOccurrenceIdAndArchivedFalse(Long occurrenceId);
}
