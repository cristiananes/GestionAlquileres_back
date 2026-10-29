package com.example.gestionalquilerback.service;

import com.example.gestionalquilerback.repository.IncomeRepository;
import com.example.gestionalquilerback.repository.ExpenseRepository;
import com.example.gestionalquilerback.security.SecurityUtil;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PeriodTotalsTest {
    private final SecurityUtil security = mock(SecurityUtil.class);
    private final IncomeRepository incomes = mock(IncomeRepository.class);
    private final ExpenseRepository expenses = mock(ExpenseRepository.class);
    private final PropertyService properties = mock(PropertyService.class);
    private final IncomeService incomeService = new IncomeService(incomes, properties, security, mock(PaymentIntegrity.class));
    private final ExpenseService expenseService = new ExpenseService(expenses, properties, security, mock(PaymentIntegrity.class));

    @Test
    void userTotalsRetainUserScopeWithOpenEndedDates() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        when(security.getCurrentUserId()).thenReturn(7L);
        when(incomes.sumForPeriod(7L, from, null)).thenReturn(BigDecimal.TEN);
        when(expenses.sumForPeriod(7L, null, from)).thenReturn(BigDecimal.ONE);
        assertEquals(BigDecimal.TEN, incomeService.total(from, null));
        assertEquals(BigDecimal.ONE, expenseService.total(null, from));
        verify(incomes).sumForPeriod(7L, from, null);
        verify(expenses).sumForPeriod(7L, null, from);
    }

    @Test
    void unfilteredTotalsKeepExistingBehaviour() {
        when(security.getCurrentUserId()).thenReturn(7L);
        when(incomes.sumAll(7L)).thenReturn(BigDecimal.TEN);
        when(expenses.sumAll(7L)).thenReturn(BigDecimal.ONE);
        assertEquals(BigDecimal.TEN, incomeService.total(null, null));
        assertEquals(BigDecimal.ONE, expenseService.total(null, null));
    }

    @Test
    void adminPeriodTotalsKeepGlobalScope() {
        when(security.isAdmin()).thenReturn(true);
        LocalDate from = LocalDate.of(2026, 10, 1);
        incomeService.total(from, null);
        expenseService.total(from, null);
        verify(incomes).sumForPeriod(null, from, null);
        verify(expenses).sumForPeriod(null, from, null);
    }
}
