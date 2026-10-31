package com.example.gestionalquilerback.controller;
import com.example.gestionalquilerback.dto.*;import com.example.gestionalquilerback.service.SettlementService;
import jakarta.validation.Valid;import lombok.RequiredArgsConstructor;import org.springframework.web.bind.annotation.*;import org.springframework.http.*;
import java.util.List;import java.io.ByteArrayOutputStream;import com.lowagie.text.*;import com.lowagie.text.pdf.PdfWriter;
@RestController @RequiredArgsConstructor @RequestMapping("/api")
public class SettlementController {
 private final SettlementService service;
 @GetMapping("/properties/{id}/management") public ManagementRequest settings(@PathVariable Long id) {return service.settings(id);}
 @PutMapping("/properties/{id}/management") public ManagementRequest settings(@PathVariable Long id,@Valid @RequestBody ManagementRequest request) {return service.saveSettings(id,request);}
 @GetMapping("/properties/{id}/settlements") public List<SettlementResponse> list(@PathVariable Long id) {return service.list(id);}
 @PostMapping("/settlements/preview") public SettlementResponse preview(@Valid @RequestBody SettlementRequest request) {return service.preview(request);}
 @PostMapping("/settlements") public SettlementResponse create(@Valid @RequestBody SettlementRequest request) {return service.create(request);}
 @PostMapping("/settlements/{id}/paid") public SettlementResponse paid(@PathVariable Long id,@Valid @RequestBody SettlementPaidRequest request) {return service.paid(id,request.date());}
 @PostMapping("/settlements/{id}/cancel") public SettlementResponse cancel(@PathVariable Long id) {return service.cancel(id);}
 @GetMapping("/settlements/{id}/pdf") public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
  var s=service.get(id);var out=new ByteArrayOutputStream();var doc=new Document();PdfWriter.getInstance(doc,out);doc.open();
  doc.add(new Paragraph("Liquidación #"+s.id()+" · "+s.status()));doc.add(new Paragraph("Resumen de gestión. No es una factura fiscal ni un justificante bancario."));
  doc.add(new Paragraph(s.propertyName()+" · "+s.ownerName()));doc.add(new Paragraph("Periodo: "+s.from()+" - "+s.to()));
  doc.add(new Paragraph("Cobro de rentas: "+(s.collectionMode()==ManagementRequest.CollectionMode.MANAGER?"Gestor":"Directamente el propietario")));
  for(var l:s.lines())doc.add(new Paragraph((l.kind().equals("INCOME")?"Ingreso ":"Gasto ")+l.movementId()+" · "+l.date()+" · "+l.description()+" · "+l.amount()+" EUR"));
  doc.add(new Paragraph("Ingresos seleccionados: "+s.totalIncome()+" EUR. Gastos adelantados seleccionados: "+s.totalExpense()+" EUR."));
  doc.add(new Paragraph("Base de rentas cobradas: "+s.feeBase()+" EUR. Honorarios: "+s.feeNet()+" EUR. Impuesto configurado: "+s.taxRate()+"% = "+s.feeTax()+" EUR."));
  doc.add(new Paragraph("Total honorarios: "+s.feeTotal()+" EUR. Neto al propietario (negativo: debe abonarlo al gestor): "+s.netToOwner()+" EUR."));
  if(s.paidDate()!=null)doc.add(new Paragraph("Registrada como saldada el "+s.paidDate()));doc.close();
  return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=liquidacion-"+id+".pdf").header(HttpHeaders.CACHE_CONTROL,"no-store").body(out.toByteArray());
 }
}
