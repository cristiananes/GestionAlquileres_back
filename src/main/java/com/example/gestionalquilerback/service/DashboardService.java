package com.example.gestionalquilerback.service;

import com.example.gestionalquilerback.dto.DashboardSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final TaskService taskService;
    private final PropertyService propertyService;
    private final CalendarEventService calendarEventService;

    public DashboardSummaryResponse getSummary() {
        BigDecimal totalIncomes = incomeService.total();
        BigDecimal totalExpenses = expenseService.total();
        BigDecimal balance = totalIncomes.subtract(totalExpenses);

        return DashboardSummaryResponse.builder()
                .totalIncomes(totalIncomes)
                .totalExpenses(totalExpenses)
                .balance(balance)
                .pendingTasks(taskService.countPending())
                .totalProperties(propertyService.count())
                .upcomingEvents(calendarEventService.findUpcoming())
                .build();
    }
}
