package com.example.gestionalquilerback.service;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardServiceTest {
    private final IncomeService incomes = mock(IncomeService.class);
    private final ExpenseService expenses = mock(ExpenseService.class);
    private final TaskService tasks = mock(TaskService.class);
    private final PropertyService properties = mock(PropertyService.class);
    private final CalendarEventService events = mock(CalendarEventService.class);
    private final DashboardService service = new DashboardService(incomes, expenses, tasks, properties, events);

    @Test
    void summaryAppliesTheSamePeriodToBothTotalsButKeepsCurrentOperationalCounts() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);
        when(incomes.total(from, to)).thenReturn(new BigDecimal("870.50"));
        when(expenses.total(from, to)).thenReturn(new BigDecimal("10.00"));
        when(properties.count()).thenReturn(2L);
        when(tasks.countPending()).thenReturn(3L);
        when(events.findUpcoming()).thenReturn(List.of());

        var result = service.getSummary(from, to);
        assertEquals(new BigDecimal("860.50"), result.getBalance());
        assertEquals(2L, result.getTotalProperties());
        assertEquals(3L, result.getPendingTasks());
        verify(incomes).total(from, to);
        verify(expenses).total(from, to);
    }

    @Test
    void rejectsInvertedDatesBeforeReadingData() {
        assertThrows(IllegalArgumentException.class, () -> service.getSummary(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 10, 1)));
        verifyNoInteractions(incomes, expenses, tasks, properties, events);
    }
}
