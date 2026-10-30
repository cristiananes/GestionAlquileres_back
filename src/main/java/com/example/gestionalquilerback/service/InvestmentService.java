package com.example.gestionalquilerback.service;
import com.example.gestionalquilerback.dto.*;
import com.example.gestionalquilerback.model.entity.*;
import com.example.gestionalquilerback.model.enums.*;
import com.example.gestionalquilerback.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.*;
import java.util.*;
@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class InvestmentService {
    private final PropertyService properties;
    private final IncomeRepository incomes;
    private final ExpenseRepository expenses;
    private final RecurringRuleRepository rules;
    static BigDecimal zero(BigDecimal n) { return n == null ? BigDecimal.ZERO : n; }
    public static BigDecimal totalInvestment(Property p) {
        if (p.getPurchasePrice() == null || p.getPurchasePrice().signum() <= 0) return null;
        return p.getPurchasePrice().add(zero(p.getPurchaseCosts())).add(zero(p.getRenovationCosts())).add(zero(p.getFurnitureCosts()));
    }
    public static BigDecimal percentage(BigDecimal result, BigDecimal invested) {
        return invested == null || invested.signum() <= 0 ? null : result.multiply(BigDecimal.valueOf(100)).divide(invested, 4, RoundingMode.HALF_UP);
    }
    public InvestmentSummary summary(Long id, LocalDate from, LocalDate to) {
        var p = properties.findEntity(id);
        DateRangeValidator.validate(from, to);
        var now = LocalDate.now();
        var investment = totalInvestment(p);
        var recordedIncomes = incomes.findByPropertyId(id).stream().filter(i -> !i.isArchived()).toList();
        var recordedExpenses = expenses.findByPropertyId(id).stream().filter(e -> !e.isArchived()).toList();
        var forecastEnd = now.plusYears(1).minusDays(1);
        BigDecimal reimbursements = BigDecimal.ZERO;
        BigDecimal gross = BigDecimal.ZERO, operating = BigDecimal.ZERO, financing = BigDecimal.ZERO;
        for (var rule : rules.findByPropertyId(id)) {
            if (rule.isArchived() || p.isArchived()) continue;
            var count = RecurrenceDates.between(rule.getStartDate(), rule.getEndDate(), rule.getFrequency(), now, forecastEnd, rule.getDayOfMonth()).size();
            var amount = rule.getAmount().multiply(BigDecimal.valueOf(count));
            if (rule.getKind() == MovementKind.INCOME && rule.getIncomeType() == IncomeType.UTILITY_REIMBURSEMENT) reimbursements = reimbursements.add(amount);
            else if (rule.getKind() == MovementKind.INCOME && rule.getIncomeType() != IncomeType.DEPOSIT) gross = gross.add(amount);
            if (rule.getKind() == MovementKind.EXPENSE) {
                if (rule.getCategory() == ExpenseCategory.FINANCING) financing = financing.add(amount);
                else if (rule.getCategory() != ExpenseCategory.INVESTMENT && rule.getCategory() != ExpenseCategory.DEPOSIT_REFUND) operating = operating.add(amount);
            }
        }
        var net = gross.add(reimbursements).subtract(operating);
        var forecast = new InvestmentSummary.Forecast(now, forecastEnd, gross, net, net.subtract(financing), gross.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP), net.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP),
                percentage(gross, investment), percentage(net, investment), percentage(gross.divide(BigDecimal.valueOf(12), 8, RoundingMode.HALF_UP), investment), percentage(net.divide(BigDecimal.valueOf(12), 8, RoundingMode.HALF_UP), investment));
        return new InvestmentSummary(investment, p.getCashInvested(), actual(from, to, p, recordedIncomes, recordedExpenses),
                actual(now.withDayOfMonth(1), now.withDayOfMonth(now.lengthOfMonth()), p, recordedIncomes, recordedExpenses),
                actual(now.withDayOfYear(1), now.withMonth(12).withDayOfMonth(31), p, recordedIncomes, recordedExpenses), forecast);
    }
    private InvestmentSummary.Actual actual(LocalDate from, LocalDate to, Property p, List<Income> incomes, List<Expense> expenses) {
        var reimbursements = BigDecimal.ZERO; var gross = BigDecimal.ZERO; var deposits = BigDecimal.ZERO; var operating = BigDecimal.ZERO; var totalExpenses = BigDecimal.ZERO;
        for (var i : incomes) if ((from == null || !i.getIncomeDate().isBefore(from)) && (to == null || !i.getIncomeDate().isAfter(to))) {
            if (i.getIncomeType() == IncomeType.UTILITY_REIMBURSEMENT) reimbursements = reimbursements.add(i.getAmount());
            else if (i.getIncomeType() == IncomeType.DEPOSIT) deposits = deposits.add(i.getAmount()); else gross = gross.add(i.getAmount());
        }
        for (var e : expenses) if ((from == null || !e.getExpenseDate().isBefore(from)) && (to == null || !e.getExpenseDate().isAfter(to))) {
            if (e.getCategory() != ExpenseCategory.DEPOSIT_REFUND) totalExpenses = totalExpenses.add(e.getAmount());
            if (e.getCategory() != ExpenseCategory.FINANCING && e.getCategory() != ExpenseCategory.INVESTMENT && e.getCategory() != ExpenseCategory.DEPOSIT_REFUND) operating = operating.add(e.getAmount());
        }
        var net = gross.add(reimbursements).subtract(operating);
        // Fianzas are liabilities: keep them out of returns and cash available to the owner.
        var cashFlow = gross.add(reimbursements).subtract(totalExpenses);
        return new InvestmentSummary.Actual(from, to, gross, deposits, reimbursements, operating, net, cashFlow, percentage(gross, totalInvestment(p)), percentage(net, totalInvestment(p)), percentage(cashFlow, p.getCashInvested()));
    }
}
