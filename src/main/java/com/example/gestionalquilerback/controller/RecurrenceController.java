package com.example.gestionalquilerback.controller;
import com.example.gestionalquilerback.dto.*;
import com.example.gestionalquilerback.service.RecurrenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.*;
import java.util.*;
@RestController @RequestMapping("/api/recurrences") @RequiredArgsConstructor
public class RecurrenceController {
    private final RecurrenceService service;
    @GetMapping public List<RecurringRuleResponse> rules(@RequestParam(required = false) Long propertyId, @RequestParam(defaultValue = "false") boolean archived) { return service.findAll(propertyId, archived); }
    @PostMapping public RecurringRuleResponse create(@Valid @RequestBody RecurringRuleRequest request) { return service.create(request); }
    @GetMapping("/occurrences") public List<OccurrenceResponse> occurrences(@RequestParam(required = false) Long propertyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) { return service.upcoming(propertyId, from, to); }
    @PostMapping("/{id}/payments") public OccurrenceResponse pay(@PathVariable Long id, @Valid @RequestBody PaymentRequest request) { return service.pay(id, request); }
    @PostMapping("/{id}/finish") public void finish(@PathVariable Long id) { service.finish(id); }
    @DeleteMapping("/{id}") public void archive(@PathVariable Long id) { service.archive(id, true); }
    @PostMapping("/{id}/restore") public void restore(@PathVariable Long id) { service.archive(id, false); }
}
