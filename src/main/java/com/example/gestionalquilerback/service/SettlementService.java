package com.example.gestionalquilerback.service;
import com.example.gestionalquilerback.dto.*;
import com.example.gestionalquilerback.dto.ManagementRequest.*;
import com.example.gestionalquilerback.model.entity.*;
import com.example.gestionalquilerback.model.enums.*;
import com.example.gestionalquilerback.repository.*;
import com.example.gestionalquilerback.security.SecurityUtil;
import com.example.gestionalquilerback.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;import java.time.temporal.ChronoUnit;import java.math.*;import java.util.*;
import java.nio.charset.StandardCharsets;import java.security.*;

@Service @RequiredArgsConstructor @Transactional
public class SettlementService {
 @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;
 private final PropertyService properties;private final PropertyRepository propertyRepository;
 private final ManagementSettingsRepository settings;private final OwnerSettlementRepository settlements;private final SettlementLineRepository lines;
 private final IncomeRepository incomes;private final ExpenseRepository expenses;private final SecurityUtil security;private final SettlementIntegrity integrity;
 public ManagementRequest settings(Long propertyId) {properties.findEntity(propertyId);return settings.findById(propertyId).map(this::settingsResponse).orElse(null);}
 private ManagementRequest settingsResponse(ManagementSettings s) {return new ManagementRequest(s.getOwnerName(),s.getOwnerEmail(),s.getFeeMode(),s.getFeeRate(),s.getTaxRate(),s.getCollectionMode());}
 private void lock(Long propertyId) {properties.findEntity(propertyId);propertyRepository.lockProperty(propertyId).orElseThrow();}
 public ManagementRequest saveSettings(Long propertyId,ManagementRequest r) {
  lock(propertyId);if(r.feeMode()==FeeMode.PERCENT_RENT&&r.feeRate().compareTo(new BigDecimal("100"))>0)throw new IllegalArgumentException("El porcentaje de honorarios no puede superar 100");
  var s=settings.findById(propertyId).orElseGet(()->ManagementSettings.builder().property(properties.findEntity(propertyId)).build());
  s.setOwnerName(r.ownerName().trim());s.setOwnerEmail(r.ownerEmail());s.setFeeMode(r.feeMode());s.setFeeRate(r.feeRate());s.setTaxRate(r.taxRate());s.setCollectionMode(r.collectionMode());settings.save(s);return settingsResponse(s);
 }
 public List<SettlementResponse> list(Long propertyId) {properties.findEntity(propertyId);return settlements.findByPropertyIdOrderByIdDesc(propertyId).stream().map(this::response).toList();}
 public SettlementResponse get(Long id) {return response(owned(id));}
 private OwnerSettlement owned(Long id) {var s=settlements.findById(id).orElseThrow(()->new ResourceNotFoundException("Liquidación",id));properties.findEntity(s.getProperty().getId());if(!s.getUser().getId().equals(security.getCurrentUserId()))throw new ResourceNotFoundException("Liquidación",id);return s;}
 public SettlementResponse preview(SettlementRequest r) {return calculate(r,false);}
 public SettlementResponse create(SettlementRequest r) {
  lock(r.propertyId());var key=hash(security.getCurrentUserId()+":"+r.clientRequestId());
  var existing=settlements.findByRequestKey(key);if(existing.isPresent()) {if(!existing.get().getRequestHash().equals(hash(r.toString())))throw new IllegalArgumentException("La operación ya existe con otros datos. Recarga las liquidaciones");return response(existing.get());}
  var p=properties.findEntity(r.propertyId());if(p.isArchived())throw new IllegalArgumentException("Restaura primero la propiedad");
  var preview=calculate(r,true);
  if(r.previewToken()==null||!r.previewToken().equals(preview.reviewToken()))throw new IllegalArgumentException("Los datos o condiciones han cambiado. Revisa de nuevo el borrador antes de confirmar");
  for(var old:settlements.findByPropertyIdOrderByIdDesc(r.propertyId())) if(!old.getStatus().equals("CANCELLED")&&!old.getPeriodTo().isBefore(r.from())&&!r.to().isBefore(old.getPeriodFrom())) throw new IllegalArgumentException("El periodo se solapa con la liquidación #"+old.getId()+". Anúlala para preparar una versión corregida");
  var s=settlements.save(OwnerSettlement.builder().property(p).user(security.getCurrentUser()).requestKey(key).requestHash(hash(r.toString())).periodFrom(r.from()).periodTo(r.to()).ownerName(preview.ownerName()).ownerEmail(preview.ownerEmail()).feeMode(preview.feeMode()).feeRate(preview.feeRate()).taxRate(preview.taxRate()).collectionMode(preview.collectionMode()).totalIncome(preview.totalIncome()).totalExpense(preview.totalExpense()).feeBase(preview.feeBase()).feeNet(preview.feeNet()).feeTax(preview.feeTax()).netToOwner(preview.netToOwner()).status("CONFIRMED").build());
  for(var item:preview.lines()) lines.save(SettlementLine.builder().settlement(s).activeKey(item.kind()+":"+item.movementId()).kind(item.kind()).movementId(item.movementId()).amount(item.amount()).movementDate(item.date()).description(item.description()).build());
  return response(s);
 }
 private SettlementResponse calculate(SettlementRequest r,boolean locking) {
  var p=properties.findEntity(r.propertyId());DateRangeValidator.validate(r.from(),r.to());
  if(ChronoUnit.DAYS.between(r.from(),r.to())>366)throw new IllegalArgumentException("La liquidación debe abarcar como máximo un año");
  if(new HashSet<>(r.incomeIds()).size()!=r.incomeIds().size()||new HashSet<>(r.expenseIds()).size()!=r.expenseIds().size())throw new IllegalArgumentException("No repitas movimientos");
  var config=settings.findById(r.propertyId()).orElseThrow(()->new IllegalArgumentException("Configura el propietario y los honorarios antes de liquidar"));
  var result=new ArrayList<SettlementResponse.Line>();var totalIncome=BigDecimal.ZERO;var rent=BigDecimal.ZERO;var totalExpense=BigDecimal.ZERO;
  for(Long id:r.incomeIds().stream().sorted().toList()) {
   var i=(locking?incomes.lockMovement(id):incomes.findById(id)).orElseThrow(()->new ResourceNotFoundException("Ingreso",id));
   if(!i.getUser().getId().equals(security.getCurrentUserId())||i.getProperty()==null||!i.getProperty().getId().equals(p.getId())||i.isArchived()||i.getIncomeType()==IncomeType.DEPOSIT||i.getIncomeDate().isBefore(r.from())||i.getIncomeDate().isAfter(r.to()))throw new IllegalArgumentException("Revisa ingresos: propiedad, fechas, estado y fianzas excluidas");
   integrity.ensureFree("INCOME",id);totalIncome=totalIncome.add(i.getAmount());if(i.getIncomeType()==IncomeType.RENT)rent=rent.add(i.getAmount());
   result.add(new SettlementResponse.Line("INCOME",id,i.getIncomeDate(),i.getDescription(),i.getAmount()));
  }
  for(Long id:r.expenseIds().stream().sorted().toList()) {
   var e=(locking?expenses.lockMovement(id):expenses.findById(id)).orElseThrow(()->new ResourceNotFoundException("Gasto",id));
   if(!e.getUser().getId().equals(security.getCurrentUserId())||e.getProperty()==null||!e.getProperty().getId().equals(p.getId())||e.isArchived()||e.getOwnerSettlementId()!=null||e.getCategory()==ExpenseCategory.DEPOSIT_REFUND||e.getExpenseDate().isBefore(r.from())||e.getExpenseDate().isAfter(r.to()))throw new IllegalArgumentException("Revisa gastos: propiedad, fechas y estado. Las fianzas y honorarios ya liquidados se excluyen");
   integrity.ensureFree("EXPENSE",id);totalExpense=totalExpense.add(e.getAmount());result.add(new SettlementResponse.Line("EXPENSE",id,e.getExpenseDate(),e.getDescription(),e.getAmount()));
  }
  var feeNet=fee(config.getFeeMode(),config.getFeeRate(),rent);var feeTax=feeNet.multiply(config.getTaxRate()).divide(new BigDecimal("100"),2,RoundingMode.HALF_UP);var feeTotal=feeNet.add(feeTax);
  if(result.isEmpty()&&feeTotal.signum()==0)throw new IllegalArgumentException("Selecciona movimientos o configura una cuota fija para preparar la liquidación");
  var net=(config.getCollectionMode()==CollectionMode.MANAGER?totalIncome:BigDecimal.ZERO).subtract(totalExpense).subtract(feeTotal);
  var token=hash(settingsResponse(config).toString()+":"+r.propertyId()+":"+r.from()+":"+r.to()+":"+result);
  return new SettlementResponse(null,p.getId(),p.getName(),r.from(),r.to(),config.getOwnerName(),config.getOwnerEmail(),config.getFeeMode(),config.getFeeRate(),config.getTaxRate(),config.getCollectionMode(),totalIncome,totalExpense,rent,feeNet,feeTax,feeTotal,net,"PREVIEW",null,null,result,token);
 }
 public static BigDecimal fee(FeeMode mode,BigDecimal rate,BigDecimal rent) {return mode==FeeMode.FIXED?rate.setScale(2,RoundingMode.HALF_UP):rent.multiply(rate).divide(new BigDecimal("100"),2,RoundingMode.HALF_UP);}
 public SettlementResponse paid(Long id,LocalDate date) {
  var s=owned(id);lock(s.getProperty().getId());
  // Re-read under the property lock so concurrent confirmations see the committed status.
  entityManager.refresh(s);
  if(s.getStatus().equals("CANCELLED"))throw new IllegalArgumentException("La liquidación está anulada");
  if(s.getStatus().equals("PAID"))return response(s);
  if(date.isAfter(LocalDate.now()))throw new IllegalArgumentException("Indica una fecha real de liquidación");
  var total=s.getFeeNet().add(s.getFeeTax());
  if(total.signum()>0)expenses.save(Expense.builder().user(s.getUser()).property(s.getProperty()).ownerSettlementId(id).amount(total).expenseDate(date).description("Honorarios de gestión · Liquidación #"+id).category(ExpenseCategory.COMMISSION).build());
  s.setStatus("PAID");s.setPaidDate(date);settlements.save(s);return response(s);
 }
 public SettlementResponse cancel(Long id) {
  var s=owned(id);lock(s.getProperty().getId());entityManager.refresh(s);if(s.getStatus().equals("CANCELLED"))return response(s);
  for(var line:lines.findBySettlementIdOrderByIdAsc(id)) {if(line.getKind().equals("INCOME"))incomes.lockMovement(line.getMovementId());else expenses.lockMovement(line.getMovementId());line.setActiveKey(null);lines.save(line);}
  expenses.findByOwnerSettlementId(id).ifPresent(e->{e.setArchived(true);expenses.save(e);});s.setStatus("CANCELLED");settlements.save(s);return response(s);
 }
 private SettlementResponse response(OwnerSettlement s) {return new SettlementResponse(s.getId(),s.getProperty().getId(),s.getProperty().getName(),s.getPeriodFrom(),s.getPeriodTo(),s.getOwnerName(),s.getOwnerEmail(),s.getFeeMode(),s.getFeeRate(),s.getTaxRate(),s.getCollectionMode(),s.getTotalIncome(),s.getTotalExpense(),s.getFeeBase(),s.getFeeNet(),s.getFeeTax(),s.getFeeNet().add(s.getFeeTax()),s.getNetToOwner(),s.getStatus(),s.getPaidDate(),expenses.findByOwnerSettlementId(s.getId()).map(Expense::getId).orElse(null),lines.findBySettlementIdOrderByIdAsc(s.getId()).stream().map(l->new SettlementResponse.Line(l.getKind(),l.getMovementId(),l.getMovementDate(),l.getDescription(),l.getAmount())).toList(),null);}
 private static String hash(String value) {try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
}
