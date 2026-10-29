package com.example.gestionalquilerback.service;
import com.example.gestionalquilerback.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
@Service @RequiredArgsConstructor
public class PaymentIntegrity {
    private final PaymentOccurrenceRepository occurrences;
    private final RecurringRuleRepository rules;
    private final IncomeRepository incomes;
    private final ExpenseRepository expenses;
    public void lock(Long occurrenceId, BigDecimal restoringAmount, boolean income) {
        if (occurrenceId == null) return;
        var occurrence = occurrences.findById(occurrenceId).orElseThrow(() -> new IllegalArgumentException("Vencimiento no encontrado"));
        rules.lockById(occurrence.getRule().getId()).orElseThrow();
        if (restoringAmount == null) return;
        var paid = income ? incomes.findByOccurrenceIdAndArchivedFalse(occurrenceId).stream().map(i -> i.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add)
                : expenses.findByOccurrenceIdAndArchivedFalse(occurrenceId).stream().map(e -> e.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (paid.add(restoringAmount).compareTo(occurrence.getAmountDue()) > 0) throw new IllegalArgumentException("Restaurar este pago superaría el importe del vencimiento. Archiva primero el pago que lo sustituyó.");
    }
}
