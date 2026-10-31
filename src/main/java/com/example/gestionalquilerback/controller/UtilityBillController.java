package com.example.gestionalquilerback.controller;
import com.example.gestionalquilerback.dto.*;
import com.example.gestionalquilerback.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import java.util.List;
@RestController @RequestMapping("/api/utility-bills") @RequiredArgsConstructor
public class UtilityBillController {
 private final UtilityBillService service;
 @GetMapping public List<UtilityBillResponse> list(@RequestParam(required=false) Long propertyId,@RequestParam(defaultValue="false") boolean archived) { return service.list(propertyId,archived); }
 @GetMapping("/{id}") public UtilityBillResponse get(@PathVariable Long id) { return service.get(id); }
 @PostMapping("/preview") public List<UtilitySplit.Part> preview(@Valid @RequestBody UtilityBillRequest request) { return service.preview(request); }
 @PostMapping(consumes="multipart/form-data") public UtilityBillResponse create(@Valid @RequestPart("request") UtilityBillRequest request,@RequestPart(value="file",required=false) MultipartFile file) throws java.io.IOException { return service.create(request,file); }
 @PostMapping("/{id}/shares/{shareId}/payments") public UtilityBillResponse pay(@PathVariable Long id,@PathVariable Long shareId,@Valid @RequestBody UtilityPaymentRequest request) { return service.pay(id,shareId,request); }
 @PostMapping("/{id}/expense") public UtilityBillResponse payExpense(@PathVariable Long id,@Valid @RequestBody UtilityPaymentRequest request) { return service.payExpense(id,request); }
 @PostMapping("/{id}/archive") public ResponseEntity<Void> archive(@PathVariable Long id) {service.archive(id,true);return ResponseEntity.noContent().build();}
 @PostMapping("/{id}/restore") public ResponseEntity<Void> restore(@PathVariable Long id) {service.archive(id,false);return ResponseEntity.noContent().build();}
}
