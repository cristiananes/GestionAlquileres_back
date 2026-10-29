package com.example.gestionalquilerback.service;
import org.junit.jupiter.api.Test;
import com.example.gestionalquilerback.model.entity.*;
import com.example.gestionalquilerback.model.enums.*;
import com.example.gestionalquilerback.repository.*;
import java.math.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class InvestmentServiceTest {
    @Test void unknownInvestmentIsNotZeroPercent() {
        assertNull(InvestmentService.totalInvestment(Property.builder().build()));
        assertNull(InvestmentService.percentage(BigDecimal.TEN,null));
        assertNull(InvestmentService.percentage(BigDecimal.TEN,BigDecimal.ZERO));
    }
    @Test void investmentIncludesAllInitialCostsAndReturnsCanBeNegative() {
        var p = Property.builder().purchasePrice(new BigDecimal("90000")).purchaseCosts(new BigDecimal("6000")).renovationCosts(new BigDecimal("3000")).furnitureCosts(new BigDecimal("1000")).build();
        assertEquals(new BigDecimal("100000"),InvestmentService.totalInvestment(p));
        assertEquals(new BigDecimal("-0.6500"),InvestmentService.percentage(new BigDecimal("-650"),new BigDecimal("100000")));
    }
    @Test void depositsAndFinancingAreNotOperatingYieldAndForecastsAreNotRecordedIncome() {
        var ps = mock(PropertyService.class); var ir = mock(IncomeRepository.class); var er = mock(ExpenseRepository.class); var rr = mock(RecurringRuleRepository.class);
        var p = Property.builder().id(1L).purchasePrice(new BigDecimal("100000")).build();
        when(ps.findEntity(1L)).thenReturn(p);
        when(ir.findByPropertyId(1L)).thenReturn(List.of(Income.builder().incomeDate(LocalDate.now()).amount(new BigDecimal("800")).incomeType(IncomeType.RENT).build(), Income.builder().incomeDate(LocalDate.now()).amount(new BigDecimal("1600")).incomeType(IncomeType.DEPOSIT).build()));
        when(er.findByPropertyId(1L)).thenReturn(List.of(Expense.builder().expenseDate(LocalDate.now()).amount(new BigDecimal("150")).category(ExpenseCategory.UTILITIES).build(),Expense.builder().expenseDate(LocalDate.now()).amount(new BigDecimal("400")).category(ExpenseCategory.FINANCING).build()));
        when(rr.findByPropertyId(1L)).thenReturn(List.of());
        var result = new InvestmentService(ps,ir,er,rr).summary(1L,null,null);
        assertEquals(0,new BigDecimal("650").compareTo(result.period().netIncome()));
        assertEquals(0,new BigDecimal("250").compareTo(result.period().cashFlow()));
        assertEquals(0,new BigDecimal("1600").compareTo(result.period().deposits()));
        assertEquals(0,BigDecimal.ZERO.compareTo(result.forecast().annualGross()));
    }
}
