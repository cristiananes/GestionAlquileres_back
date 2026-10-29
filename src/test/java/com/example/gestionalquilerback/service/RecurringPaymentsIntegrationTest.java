package com.example.gestionalquilerback.service;
import com.example.gestionalquilerback.dto.*;
import com.example.gestionalquilerback.model.entity.*;
import com.example.gestionalquilerback.model.enums.*;
import com.example.gestionalquilerback.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.math.*;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest @Transactional
class RecurringPaymentsIntegrationTest {
    @Autowired RecurrenceService service;
    @Autowired UserRepository users;
    @Autowired IncomeRepository incomes;
    @Autowired IncomeService incomeService;
    @BeforeEach void user() {
        var user = users.save(User.builder().name("Owner").email("recurring@test.example").password("test").role(Role.USER).build());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,null,java.util.List.of()));
    }
    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }
    @Test void forecastsDoNotCountAsIncomeAndPartialConfirmationsAreIdempotent() {
        var today = LocalDate.now();
        var rule = service.create(RecurringRuleRequest.builder().kind(MovementKind.INCOME).frequency(RecurrenceFrequency.MONTHLY).amount(new BigDecimal("100")).description("Rent").startDate(today).incomeType(IncomeType.RENT).clientRequestId("rule-test").build());
        var request = PaymentRequest.builder().dueDate(today).paymentDate(today).amount(new BigDecimal("40")).clientRequestId("payment-test").build();
        assertEquals(0,incomes.count());
        assertEquals("PENDING",service.upcoming(null,today,today).getFirst().status());
        assertEquals("PARTIAL",service.pay(rule.id(),request).status());
        service.pay(rule.id(),request);
        assertEquals(1,incomes.count());
        assertEquals(0,new BigDecimal("60").compareTo(service.upcoming(null,today,today).getFirst().remaining()));
        request.setAmount(new BigDecimal("60")); request.setClientRequestId("payment-test-2");
        assertEquals("PAID",service.pay(rule.id(),request).status());
        request.setClientRequestId("payment-test-3");
        assertThrows(IllegalArgumentException.class,()->service.pay(rule.id(),request));
    }
    @Test void anotherUserCannotConfirmAnOwnersOccurrence() {
        var today = LocalDate.now();
        var rule = service.create(RecurringRuleRequest.builder().kind(MovementKind.INCOME).frequency(RecurrenceFrequency.MONTHLY).amount(BigDecimal.TEN).description("Rent").startDate(today).incomeType(IncomeType.RENT).clientRequestId("private-rule").build());
        var other = users.save(User.builder().name("Other").email("other-recurring@test.example").password("test").role(Role.USER).build());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(other,null,java.util.List.of()));
        assertThrows(com.example.gestionalquilerback.exception.ResourceNotFoundException.class,()->service.pay(rule.id(),PaymentRequest.builder().dueDate(today).paymentDate(today).amount(BigDecimal.TEN).clientRequestId("not-owner").build()));
        assertEquals(0,service.upcoming(null,today,today).size());
    }
    @Test void archivingReopensTheDebtAndRestoreCannotOverpayIt() {
        var today = LocalDate.now();
        var rule = service.create(RecurringRuleRequest.builder().kind(MovementKind.INCOME).frequency(RecurrenceFrequency.MONTHLY).amount(new BigDecimal("100")).description("Rent").startDate(today).incomeType(IncomeType.RENT).clientRequestId("archive-rule").build());
        service.pay(rule.id(), PaymentRequest.builder().dueDate(today).paymentDate(today).amount(new BigDecimal("100")).clientRequestId("archive-payment").build());
        var original = incomes.findAll().getFirst();
        incomeService.delete(original.getId());
        assertEquals(0,new BigDecimal("100").compareTo(service.upcoming(null,today,today).getFirst().remaining()));
        service.pay(rule.id(), PaymentRequest.builder().dueDate(today).paymentDate(today).amount(new BigDecimal("100")).clientRequestId("replacement-payment").build());
        assertThrows(IllegalArgumentException.class,()->incomeService.restore(original.getId()));
    }
    @Test void finishingARulePreservesAdvancePaymentsAndTheirRetry() {
        var future = LocalDate.now().plusMonths(1);
        var rule = service.create(RecurringRuleRequest.builder().kind(MovementKind.INCOME).frequency(RecurrenceFrequency.MONTHLY).amount(BigDecimal.TEN).description("Advance rent").startDate(future).incomeType(IncomeType.RENT).clientRequestId("advance-rule").build());
        var req = PaymentRequest.builder().dueDate(future).paymentDate(LocalDate.now()).amount(BigDecimal.TEN).clientRequestId("advance-payment").build();
        service.pay(rule.id(),req);
        service.finish(rule.id());
        assertEquals("PAID",service.pay(rule.id(),req).status());
        assertEquals(1,incomes.count());
        assertEquals("PAID",service.upcoming(null,future,future).getFirst().status());
    }
}
