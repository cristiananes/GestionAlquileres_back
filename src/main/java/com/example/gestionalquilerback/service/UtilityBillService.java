package com.example.gestionalquilerback.service;
import com.example.gestionalquilerback.dto.*;
import com.example.gestionalquilerback.model.entity.*;
import com.example.gestionalquilerback.model.enums.*;
import com.example.gestionalquilerback.repository.*;
import com.example.gestionalquilerback.security.SecurityUtil;
import com.example.gestionalquilerback.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.HexFormat;

@Service @RequiredArgsConstructor @Transactional
public class UtilityBillService {
 private final UtilityBillRepository bills;
 private final UtilityShareRepository shares;
 private final IncomeRepository incomes;
 private final ExpenseRepository expenses;
 private final PropertyService properties;
 private final DocumentService documents;
 private final SecurityUtil security;
 public List<UtilityBillResponse> list(Long propertyId,boolean archived) {
  if(propertyId!=null) properties.findEntity(propertyId);
  return bills.findByUserIdOrderByIdDesc(security.getCurrentUserId()).stream().filter(b->b.isArchived()==archived && (propertyId==null||propertyId.equals(b.getProperty().getId()))).map(this::response).toList();
 }
 public List<UtilitySplit.Part> preview(UtilityBillRequest r) { properties.findEntity(r.propertyId()); return UtilitySplit.calculate(r); }
 public UtilityBillResponse create(UtilityBillRequest r,MultipartFile file) throws java.io.IOException {
  var property=properties.findEntity(r.propertyId()); if(property.isArchived()) throw new IllegalArgumentException("Restaura primero la propiedad");
  var key=hash(security.getCurrentUserId()+":"+r.clientRequestId());
  var existing=bills.findByClientRequestIdAndUserId(key,security.getCurrentUserId()); if(existing.isPresent()) { if(!hash(r.toString()).equals(existing.get().getRequestHash())) throw new IllegalArgumentException("La operación ya se guardó con otros datos. Recarga los recibos antes de reintentar."); return response(existing.get()); }
  var parts=UtilitySplit.calculate(r); var fingerprint=fingerprint(r.propertyId(),r.provider(),r.reference());
  if(bills.findByFingerprint(fingerprint).isPresent()) throw new IllegalArgumentException("Ya hay un recibo con este proveedor y referencia en la propiedad");
  var b=UtilityBill.builder().user(security.getCurrentUser()).property(property).clientRequestId(key).fingerprint(fingerprint).requestHash(hash(r.toString()))
   .supply(r.supply().trim()).provider(r.provider().trim()).reference(r.reference().trim()).amount(r.amount()).periodFrom(r.periodFrom()).periodTo(r.periodTo()).dueDate(r.dueDate())
   .method(r.method()).roomCount(r.roomCount()).vacancy(r.vacancy()).build();
  if(file!=null) { var doc=documents.upload(property.getId(),null,null,file); b.setDocument(documents.entity(doc.id())); }
  bills.saveAndFlush(b);
  for(var part:parts) {var person=part.recipient(); shares.save(UtilityShare.builder().bill(b).name(person.name().trim()).room(person.room()).email(person.email()).phone(person.phone()).roomNumber(person.roomNumber())
   .stayFrom(person.from()).stayTo(person.to()).percent(person.percent()).owner(person.owner()).amount(part.amount()).build()); }
  return response(b);
 }
 public UtilityBillResponse get(Long id) { return response(owned(id,false)); }
 private UtilityBill owned(Long id,boolean lock) {
  var b=(lock?bills.lockById(id):bills.findById(id)).orElseThrow(()->new ResourceNotFoundException("Recibo",id));
  if(!b.getUser().getId().equals(security.getCurrentUserId())) throw new ResourceNotFoundException("Recibo",id);
  return b;
 }
 public UtilityBillResponse pay(Long billId,Long shareId,UtilityPaymentRequest r) {
  var b=owned(billId,true); active(b);
  var s=shares.findById(shareId).filter(x->x.getBill().getId().equals(billId)).orElseThrow(()->new ResourceNotFoundException("Reparto",shareId));
  if(s.isOwner()) throw new IllegalArgumentException("La parte del propietario no genera un reembolso");
  if(r.date().isAfter(LocalDate.now())) throw new IllegalArgumentException("Indica la fecha real del cobro");
  var key=hash("utility-payment:"+security.getCurrentUserId()+":"+r.clientRequestId());
  var retry=incomes.findByClientRequestIdAndUserId(key,security.getCurrentUserId());
  if(retry.isPresent()) {
   var i=retry.get(); if(!shareId.equals(i.getUtilityShareId())||i.getAmount().compareTo(r.amount())!=0||!i.getIncomeDate().equals(r.date())||i.isArchived()) throw new IllegalArgumentException("El reintento tiene otros datos o el cobro ya está archivado. Recarga y revisa los pagos.");
   return response(b);
  }
  if(r.movementId()!=null) {
   var i=incomes.lockMovement(r.movementId()).orElseThrow(()->new ResourceNotFoundException("Ingreso",r.movementId()));
   if(shareId.equals(i.getUtilityShareId()) && i.getUser().getId().equals(security.getCurrentUserId()) && !i.isArchived() && i.getAmount().compareTo(r.amount())==0 && i.getIncomeDate().equals(r.date())) return response(b);
   if(!i.getUser().getId().equals(security.getCurrentUserId())||i.getProperty()==null||!b.getProperty().getId().equals(i.getProperty().getId())||i.isArchived()||i.getUtilityShareId()!=null||i.getOccurrenceId()!=null||i.getAmount().compareTo(r.amount())!=0||!i.getIncomeDate().equals(r.date())||(i.getIncomeType()!=IncomeType.OTHER && i.getIncomeType()!=IncomeType.UTILITY_REIMBURSEMENT)) throw new IllegalArgumentException("Selecciona un ingreso puntual de esta propiedad, tipo Otro o Reembolso, con el mismo importe y fecha, sin vincular y no archivado");
   checkPending(s,r.amount()); i.setUtilityShareId(shareId); i.setIncomeType(IncomeType.UTILITY_REIMBURSEMENT); incomes.save(i);
  } else {
   checkPending(s,r.amount()); incomes.save(Income.builder().user(b.getUser()).property(b.getProperty()).utilityShareId(shareId).clientRequestId(key).amount(r.amount()).incomeDate(r.date())
    .description(shortDescription("Reembolso "+b.getSupply()+" · "+b.getReference()+" · "+s.getName())).incomeType(IncomeType.UTILITY_REIMBURSEMENT).paymentMethod(r.paymentMethod()==null?PaymentMethod.TRANSFER:r.paymentMethod()).build());
  }
  return response(b);
 }
 private void checkPending(UtilityShare s,BigDecimal amount) { if(amount.signum()<=0 || paid(s).add(amount).compareTo(s.getAmount())>0) throw new IllegalArgumentException("El cobro supera el importe pendiente del destinatario"); }
 public UtilityBillResponse payExpense(Long id,UtilityPaymentRequest r) {
  var b=owned(id,true); active(b);
  if(!r.amount().equals(b.getAmount()) && r.amount().compareTo(b.getAmount())!=0) throw new IllegalArgumentException("El gasto debe coincidir con el total de la factura");
  if(r.date().isAfter(LocalDate.now())) throw new IllegalArgumentException("Indica la fecha real del pago");
  var prior=expenses.findByUtilityBillId(id);
  if(prior.isPresent()) { if(prior.get().isArchived()) throw new IllegalArgumentException("El gasto vinculado está archivado. Restáuralo desde Archivados para recuperarlo sin duplicarlo."); return response(b); }
  Expense e;
  if(r.movementId()!=null) {
   e=expenses.lockMovement(r.movementId()).orElseThrow(()->new ResourceNotFoundException("Gasto",r.movementId()));
   if(!e.getUser().getId().equals(security.getCurrentUserId())||e.getProperty()==null||!b.getProperty().getId().equals(e.getProperty().getId())||e.isArchived()||e.getUtilityBillId()!=null||e.getOccurrenceId()!=null||e.getAmount().compareTo(b.getAmount())!=0||!e.getExpenseDate().equals(r.date())||e.getCategory()!=ExpenseCategory.UTILITIES) throw new IllegalArgumentException("Selecciona un gasto puntual de Suministros, de esta propiedad, con el mismo importe y fecha, sin vincular y no archivado");
  } else e=Expense.builder().user(b.getUser()).property(b.getProperty()).clientRequestId(hash("utility-expense:"+id)).amount(b.getAmount()).expenseDate(r.date()).category(ExpenseCategory.UTILITIES).description(b.getSupply()+" · "+b.getProvider()+" · "+b.getReference()).build();
  e.setUtilityBillId(id); expenses.save(e); return response(b);
 }
 public void archive(Long id,boolean archived) {
  var b=owned(id,true);
  if(archived==b.isArchived()) return;
  if(archived) {
   if(shares.findByBillIdOrderByIdAsc(id).stream().anyMatch(s->paid(s).signum()>0)||expenses.findByUtilityBillId(id).filter(e->!e.isArchived()).isPresent()) throw new IllegalArgumentException("Archiva primero los cobros y el gasto vinculados para corregir el recibo sin ocultar movimientos reales");
   b.setFingerprint(hash("archived-bill:"+id));
  } else {
   var key=fingerprint(b.getProperty().getId(),b.getProvider(),b.getReference());
   if(bills.findByFingerprint(key).isPresent()) throw new IllegalArgumentException("Ya existe un recibo activo con esta referencia. Revisa la versión corregida antes de restaurar");
   b.setFingerprint(key);
  }
  b.setArchived(archived); bills.save(b);
 }
 private void active(UtilityBill b) { if(b.isArchived()||b.getProperty().isArchived()) throw new IllegalArgumentException("Restaura el recibo y su propiedad antes de registrar pagos"); }
 private BigDecimal paid(UtilityShare s) { return incomes.findByUtilityShareId(s.getId()).stream().filter(i->!i.isArchived()).map(Income::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add); }
 private UtilityBillResponse response(UtilityBill b) {
  var expense=expenses.findByUtilityBillId(b.getId()).orElse(null);
  var parts=shares.findByBillIdOrderByIdAsc(b.getId()).stream().map(s->{
   var payments=incomes.findByUtilityShareId(s.getId()).stream().filter(i->!i.isArchived()).map(i->new UtilityBillResponse.Payment(i.getId(),i.getAmount(),i.getIncomeDate())).toList();
   var paid=payments.stream().map(UtilityBillResponse.Payment::amount).reduce(BigDecimal.ZERO,BigDecimal::add); var pending=s.getAmount().subtract(paid);
   var status=s.isOwner()?"OWNER":pending.signum()==0?"PAID":paid.signum()>0?"PARTIAL":b.getDueDate().isBefore(LocalDate.now())?"OVERDUE":"PENDING";
   return new UtilityBillResponse.Share(s.getId(),s.getName(),s.getRoom(),s.getEmail(),s.getPhone(),s.getRoomNumber(),s.getStayFrom(),s.getStayTo(),s.getPercent(),s.isOwner(),s.getAmount(),paid,s.isOwner()?BigDecimal.ZERO:pending,status,payments);
  }).toList();
  return new UtilityBillResponse(b.getId(),b.getProperty().getId(),b.getProperty().getName(),b.getSupply(),b.getProvider(),b.getReference(),b.getAmount(),b.getPeriodFrom(),b.getPeriodTo(),b.getDueDate(),b.getMethod(),b.getRoomCount(),b.getVacancy(),b.getDocument()==null?null:b.getDocument().getId(),b.getDocument()==null?null:b.getDocument().getFilename(),b.isArchived(),expense==null?null:expense.getId(),expense!=null&&!expense.isArchived(),parts);
 }
 private String fingerprint(Long property,String provider,String reference) {return hash(security.getCurrentUserId()+":"+property+":"+provider.trim().toLowerCase(Locale.ROOT)+":"+reference.trim().toLowerCase(Locale.ROOT));}
 private static String shortDescription(String value) {return value.substring(0,Math.min(value.length(),250));}
 private static String hash(String text) {try {return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
}
