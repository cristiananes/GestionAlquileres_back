package com.example.gestionalquilerback.service;

import com.example.gestionalquilerback.model.entity.*;
import com.example.gestionalquilerback.model.enums.*;
import com.example.gestionalquilerback.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PeriodQueriesTest {
    @Autowired IncomeRepository incomes;
    @Autowired ExpenseRepository expenses;
    @Autowired UserRepository users;

    @Test
    void sumsAreInclusiveSupportNullBoundsAndIsolateUsers() {
        var owner = users.save(User.builder().name("Owner").email("owner@test.example").password("test").role(Role.USER).build());
        var other = users.save(User.builder().name("Other").email("other@test.example").password("test").role(Role.USER).build());
        var start = LocalDate.of(2026, 10, 1);
        var end = LocalDate.of(2026, 10, 31);
        for (var day : new LocalDate[] { start.minusDays(1), start, end, end.plusDays(1) }) {
            incomes.save(Income.builder().user(owner).amount(BigDecimal.TEN).description("Rent").incomeDate(day).incomeType(IncomeType.RENT).build());
            expenses.save(Expense.builder().user(owner).amount(BigDecimal.ONE).description("Bill").expenseDate(day).category(ExpenseCategory.UTILITIES).build());
        }
        incomes.save(Income.builder().user(other).amount(new BigDecimal("100")).description("Other rent").incomeDate(start).incomeType(IncomeType.RENT).build());
        expenses.save(Expense.builder().user(other).amount(BigDecimal.TEN).description("Other bill").expenseDate(start).category(ExpenseCategory.UTILITIES).build());
        assertMoney("20", incomes.sumForPeriod(owner.getId(), start, end));
        assertMoney("2", expenses.sumForPeriod(owner.getId(), start, end));
        assertMoney("30", incomes.sumForPeriod(owner.getId(), start, null));
        assertMoney("3", expenses.sumForPeriod(owner.getId(), null, end));
        assertMoney("120", incomes.sumForPeriod(null, start, end));
        assertMoney("12", expenses.sumForPeriod(null, start, end));
        assertMoney("0", incomes.sumForPeriod(owner.getId(), end.plusDays(2), null));
    }

    private void assertMoney(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
