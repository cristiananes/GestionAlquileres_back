package com.example.gestionalquilerback.service;
import com.example.gestionalquilerback.dto.*;
import com.example.gestionalquilerback.dto.UtilityBillRequest.*;
import com.example.gestionalquilerback.model.entity.*;
import com.example.gestionalquilerback.model.enums.*;
import com.example.gestionalquilerback.repository.*;
import com.example.gestionalquilerback.exception.ResourceNotFoundException;
import java.time.*;
import java.math.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.mock.web.MockMultipartFile;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest @Transactional
class UtilityBillsIntegrationTest {
 @Autowired UtilityBillService service;
 @Autowired UserRepository users;
 @Autowired PropertyRepository properties;
 @Autowired IncomeRepository incomes;
 @Autowired ExpenseRepository expenses;
 @Autowired IncomeService incomeService;
 @Autowired ExpenseService expenseService;
 @Autowired InvestmentService investment;
 @Autowired DocumentService documents;
 Property property; User owner;
 void login(User u) {SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(u,null,List.of()));}
 @BeforeEach void setup() {owner=users.save(User.builder().name("Owner").email("utility-owner@test.example").password("test").role(Role.USER).build());login(owner); property=properties.save(Property.builder().user(owner).name("Piso").address("Calle").purchasePrice(new BigDecimal("100000")).build());}
 @AfterEach void logout() {SecurityContextHolder.clearContext();}
 UtilityBillRequest request(String key,String reference) {return new UtilityBillRequest(property.getId(),key,"Luz","Proveedor",reference,new BigDecimal("60.00"),LocalDate.now().minusDays(30),LocalDate.now(),LocalDate.now(),Method.EQUAL,2,Vacancy.OWNER,List.of(new Recipient("Ana","1",null,null,null,null,null,null,false),new Recipient("Luis","2",null,null,null,null,null,null,false)));}
 UtilityPaymentRequest payment(String key,String amount,Long movement) {return new UtilityPaymentRequest(new BigDecimal(amount),LocalDate.now(),key,movement);}
 @Test void reviewDoesNotCreateCashAndPaymentsArePartialIdempotentAndBounded() throws Exception {
  var req=request("create","ref1"); assertEquals(2,service.preview(req).size()); assertEquals(0,incomes.count());
  var b=service.create(req,new MockMultipartFile("file","luz.pdf","application/pdf","%PDF-1.7".getBytes())); assertEquals(b.id(),service.create(req,null).id());
  assertEquals(0,incomes.count()); assertEquals(0,expenses.count());
  var share=b.shares().getFirst().id();
  service.pay(b.id(),share,payment("partial","10.00",null)); service.pay(b.id(),share,payment("partial","10.00",null)); assertEquals(1,incomes.count());
  assertThrows(IllegalArgumentException.class,()->service.pay(b.id(),share,payment("too-much","21.00",null)));
  var complete=service.pay(b.id(),share,payment("rest","20.00",null)); assertEquals("PAID",complete.shares().getFirst().status());
  assertThrows(IllegalArgumentException.class,()->service.archive(b.id(),true));
  service.payExpense(b.id(),payment("expense","60.00",null)); service.payExpense(b.id(),payment("expense","60.00",null)); assertEquals(1,expenses.count());
  var result=investment.summary(property.getId(),null,null).period(); assertEquals(0,result.grossIncome().signum()); assertEquals(0,result.reimbursements().compareTo(new BigDecimal("30"))); assertEquals(0,result.netIncome().compareTo(new BigDecimal("-30")));
  var original=complete.shares().getFirst().payments().getFirst().incomeId(); incomeService.delete(original);
  assertEquals("PARTIAL",service.get(b.id()).shares().getFirst().status());
  service.pay(b.id(),share,payment("replacement","10.00",null));
  assertThrows(IllegalArgumentException.class,()->incomeService.restore(original));
 }
 @Test void existingMovementsCanBeLinkedWithoutDuplicatingCashAndAreProtected() throws Exception {
  var b=service.create(request("link","ref2"),null); var share=b.shares().getFirst().id();
  var i=incomes.save(Income.builder().user(owner).property(property).amount(new BigDecimal("30.00")).incomeDate(LocalDate.now()).description("Manual").incomeType(IncomeType.OTHER).build());
  service.pay(b.id(),share,payment("link-income","30.00",i.getId())); service.pay(b.id(),share,payment("retry-link","30.00",i.getId())); assertEquals(1,incomes.count());
  assertThrows(IllegalArgumentException.class,()->incomeService.update(i.getId(),IncomeRequest.builder().build()));
  var e=expenses.save(Expense.builder().user(owner).property(property).amount(new BigDecimal("60.00")).expenseDate(LocalDate.now()).description("Manual").category(ExpenseCategory.UTILITIES).build());
  service.payExpense(b.id(),payment("link-expense","60.00",e.getId())); assertEquals(1,expenses.count());
  assertThrows(IllegalArgumentException.class,()->expenseService.update(e.getId(),ExpenseRequest.builder().build()));
  incomeService.delete(i.getId());expenseService.delete(e.getId());service.archive(b.id(),true);
  assertThrows(IllegalArgumentException.class,()->incomeService.restore(i.getId()));assertThrows(IllegalArgumentException.class,()->expenseService.restore(e.getId()));
  service.archive(b.id(),false);incomeService.restore(i.getId());expenseService.restore(e.getId());
 }
 @Test void anotherOwnerCannotReadCollectOrDownloadAndDuplicateInvoiceIsRejected() throws Exception {
  var b=service.create(request("private","ref3"),new MockMultipartFile("file","private.pdf","application/pdf","%PDF-1.7".getBytes()));
  assertThrows(IllegalArgumentException.class,()->service.create(request("duplicate","ref3"),null));
  var other=users.save(User.builder().name("Other").email("utility-other@test.example").password("test").role(Role.USER).build());login(other);
  assertTrue(service.list(null,false).isEmpty());assertThrows(ResourceNotFoundException.class,()->service.get(b.id()));
  assertThrows(ResourceNotFoundException.class,()->service.pay(b.id(),b.shares().getFirst().id(),payment("steal","1.00",null)));
  assertThrows(ResourceNotFoundException.class,()->documents.entity(b.documentId()));
 }
}
