package com.example.gestionalquilerback.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardSummaryResponse {
    private BigDecimal totalIncomes;
    private BigDecimal totalExpenses;
    private BigDecimal balance;
    private long pendingTasks;
    private long totalProperties;
    private List<CalendarEventResponse> upcomingEvents;
}
