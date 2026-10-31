package com.example.gestionalquilerback.service;
import com.example.gestionalquilerback.dto.*;import com.example.gestionalquilerback.dto.ManagementRequest.*;
import com.example.gestionalquilerback.model.entity.*;import com.example.gestionalquilerback.model.enums.*;import com.example.gestionalquilerback.repository.*;
import org.junit.jupiter.api.*;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.test.context.SpringBootTest;import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.math.*;import java.time.*;import java.util.*;import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest @Transactional
class RoomsAndSettlementsIntegrationTest {
 @Autowired RoomService rooms;@Autowired LeaseService leases;@Autowired SettlementService settlements;
 @Autowired UserRepository users;@Autowired PropertyRepository properties;@Autowired IncomeRepository incomes;@Autowired ExpenseRepository expenses;
 @Autowired IncomeService incomeService;@Autowired ExpenseService expenseService;
 Property property;User user;LocalDate from=LocalDate.now().withDayOfMonth(1),to=LocalDate.now();
 @BeforeEach void setup(){user=users.save(User.builder().name("Gestor").email("rooms-settlements@test.example").password("test").role(Role.USER).build());login(user);property=properties.save(Property.builder().user(user).name("Piso").address("Calle").build());}
 void login(User u){SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(u,null,List.of()));}
 @AfterEach void cleanup(){SecurityContextHolder.clearContext();}
 LeaseRequest lease(String key,Long room,LocalDate start,LocalDate end){return LeaseRequest.builder().tenantName(key).startDate(start).endDate(end).monthlyRent(new BigDecimal("300")).paymentDay(1).clientRequestId(key).rentalScope(room==null?RentalScope.ENTIRE_PROPERTY:RentalScope.ROOM).roomId(room).build();}
 @Test void occupancyRejectsOverlapAcrossRoomAndWholePropertyAndPreservesHistory(){
  var a=rooms.create(property.getId(),new RoomRequest("Azul",1));var b=rooms.create(property.getId(),new RoomRequest("Verde",2));
  var first=leases.create(property.getId(),lease("A",a.id(),from,from.plusDays(5)));
  assertThrows(IllegalArgumentException.class,()->leases.create(property.getId(),lease("overlap",a.id(),from.plusDays(5),from.plusDays(8))));
  leases.create(property.getId(),lease("B",b.id(),from,from.plusDays(10)));
  assertThrows(IllegalArgumentException.class,()->leases.create(property.getId(),lease("whole",null,from,from.plusDays(10))));
  leases.create(property.getId(),lease("next",a.id(),from.plusDays(6),from.plusDays(9)));
  assertEquals("OCCUPIED",rooms.list(property.getId(),false,from).getFirst().status());
  assertEquals("AVAILABLE",rooms.list(property.getId(),false,from.minusDays(1)).getFirst().status());
  leases.archive(first.id(),true);assertEquals("AVAILABLE",rooms.list(property.getId(),false,from).getFirst().status());
  leases.archive(first.id(),false);assertEquals("OCCUPIED",rooms.list(property.getId(),false,from).getFirst().status());
 }
 @Test void legacyContractsCanBeAssignedAndForeignRoomsAreRejected(){
  var req=lease("legacy",null,from,null);req.setRentalScope(null);var old=leases.create(property.getId(),req);assertEquals(RentalScope.UNASSIGNED,old.rentalScope());
  var r=rooms.create(property.getId(),new RoomRequest("Uno",1));var assigned=leases.assign(old.id(),new OccupancyRequest(RentalScope.ROOM,r.id(),from,null));assertEquals(r.id(),assigned.roomId());
  var other=users.save(User.builder().name("Otro").email("rooms-other@test.example").password("test").role(Role.USER).build());login(other);
  assertThrows(com.example.gestionalquilerback.exception.ResourceNotFoundException.class,()->rooms.list(property.getId(),false,to));
 }
 Income income(IncomeType type,String amount){return incomes.save(Income.builder().user(user).property(property).amount(new BigDecimal(amount)).description(type.name()).incomeType(type).incomeDate(to).build());}
 SettlementRequest request(List<Long> incomeIds,List<Long> expenseIds,String key,String token){return new SettlementRequest(property.getId(),from,to,incomeIds,expenseIds,key,token);}
 @Test void selectedCashIsFrozenFeesExcludeReimbursementsAndSettlementDoesNotTransferMoney(){
  settlements.saveSettings(property.getId(),new ManagementRequest("Propietario",null,FeeMode.PERCENT_RENT,new BigDecimal("10"),new BigDecimal("21"),CollectionMode.MANAGER));
  var rent=income(IncomeType.RENT,"1000");var reimb=income(IncomeType.UTILITY_REIMBURSEMENT,"100");
  var e=expenses.save(Expense.builder().user(user).property(property).amount(new BigDecimal("100")).description("Luz").category(ExpenseCategory.UTILITIES).expenseDate(to).build());
  var preview=settlements.preview(request(List.of(rent.getId(),reimb.getId()),List.of(e.getId()),"first",null));
  assertEquals(0,preview.feeNet().compareTo(new BigDecimal("100")));assertEquals(0,preview.feeTax().compareTo(new BigDecimal("21")));assertEquals(0,preview.netToOwner().compareTo(new BigDecimal("879")));
  var req=request(List.of(rent.getId(),reimb.getId()),List.of(e.getId()),"first",preview.reviewToken());var s=settlements.create(req);assertEquals(s.id(),settlements.create(req).id());assertEquals(1,expenses.count());
  assertThrows(IllegalArgumentException.class,()->incomeService.delete(rent.getId()));assertThrows(IllegalArgumentException.class,()->expenseService.delete(e.getId()));
  settlements.paid(s.id(),to);settlements.paid(s.id(),to);assertEquals(2,expenses.count());var fee=expenses.findByOwnerSettlementId(s.id()).orElseThrow();assertEquals(0,fee.getAmount().compareTo(new BigDecimal("121")));
  assertThrows(IllegalArgumentException.class,()->expenseService.delete(fee.getId()));
  settlements.cancel(s.id());assertTrue(expenses.findById(fee.getId()).orElseThrow().isArchived());incomeService.delete(rent.getId());assertEquals("CANCELLED",settlements.get(s.id()).status());
 }
 @Test void directOwnerCollectionDoesNotPayOutRentAgainAndChangedConditionsRequireReview(){
  settlements.saveSettings(property.getId(),new ManagementRequest("Owner",null,FeeMode.FIXED,new BigDecimal("50"),BigDecimal.ZERO,CollectionMode.OWNER));var rent=income(IncomeType.RENT,"1000");
  var p=settlements.preview(request(List.of(rent.getId()),List.of(),"direct",null));assertEquals(0,p.netToOwner().compareTo(new BigDecimal("-50")));
  settlements.saveSettings(property.getId(),new ManagementRequest("Owner",null,FeeMode.FIXED,new BigDecimal("60"),BigDecimal.ZERO,CollectionMode.OWNER));
  assertThrows(IllegalArgumentException.class,()->settlements.create(request(List.of(rent.getId()),List.of(),"direct",p.reviewToken())));
  var deposit=income(IncomeType.DEPOSIT,"1000");assertThrows(IllegalArgumentException.class,()->settlements.preview(request(List.of(deposit.getId()),List.of(),"deposit",null)));
 }
}
