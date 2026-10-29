package com.example.gestionalquilerback.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
public record InvestmentSummary(BigDecimal totalInvestment, BigDecimal cashInvested, Actual period, Actual month, Actual year, Forecast forecast) {
    public record Actual(LocalDate from, LocalDate to, BigDecimal grossIncome, BigDecimal deposits, BigDecimal operatingExpenses, BigDecimal netIncome, BigDecimal cashFlow, BigDecimal grossReturn, BigDecimal netReturn, BigDecimal cashReturn) {}
    public record Forecast(LocalDate from, LocalDate to, BigDecimal annualGross, BigDecimal annualNet, BigDecimal annualCashFlow, BigDecimal monthlyGross, BigDecimal monthlyNet, BigDecimal grossAnnualReturn, BigDecimal netAnnualReturn, BigDecimal grossMonthlyReturn, BigDecimal netMonthlyReturn) {}
}
