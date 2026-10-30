package com.example.gestionalquilerback.service;

import com.example.gestionalquilerback.dto.ExpenseRequest;
import com.example.gestionalquilerback.dto.ExpenseResponse;
import com.example.gestionalquilerback.exception.ResourceNotFoundException;
import com.example.gestionalquilerback.model.entity.Expense;
import com.example.gestionalquilerback.repository.ExpenseRepository;
import com.example.gestionalquilerback.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@org.springframework.transaction.annotation.Transactional
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository repository;
    private final PropertyService propertyService;
    private final SecurityUtil securityUtil;
    private final PaymentIntegrity paymentIntegrity;
    private final com.example.gestionalquilerback.repository.UtilityBillRepository utilityBills;

    public List<ExpenseResponse> findAll(Long propertyId, LocalDate from, LocalDate to) {
        return findAll(propertyId, from, to, false);
    }

    public List<ExpenseResponse> findAll(Long propertyId, LocalDate from, LocalDate to, boolean archived) {
        DateRangeValidator.validate(from, to);
        List<Expense> expenses;
        if (securityUtil.isAdmin()) {
            expenses = repository.findAll();
        } else {
            expenses = repository.findByUserId(securityUtil.getCurrentUserId());
        }
        return expenses.stream()
                .filter(e -> e.isArchived() == archived)
                .filter(e -> propertyId == null || (e.getProperty() != null && e.getProperty().getId().equals(propertyId)))
                .filter(e -> from == null || !e.getExpenseDate().isBefore(from))
                .filter(e -> to == null || !e.getExpenseDate().isAfter(to))
                .map(this::toResponse)
                .toList();
    }

    public ExpenseResponse findById(Long id) {
        return toResponse(findEntity(id));
    }

    public ExpenseResponse create(ExpenseRequest request) {
        if (request.getExpenseDate().isAfter(LocalDate.now())) throw new IllegalArgumentException("Para fechas futuras usa un vencimiento previsto en Recurrencias");
        if (request.getClientRequestId() != null) {
            var existing = repository.findByClientRequestIdAndUserId(request.getClientRequestId(), securityUtil.getCurrentUserId());
            if (existing.isPresent()) return toResponse(existing.get());
        }
        Expense entity = Expense.builder()
                .clientRequestId(request.getClientRequestId())
                .property(request.getPropertyId() != null ? propertyService.findEntity(request.getPropertyId()) : null)
                .amount(request.getAmount())
                .description(request.getDescription())
                .expenseDate(request.getExpenseDate())
                .category(request.getCategory())
                .user(securityUtil.getCurrentUser())
                .build();
        return toResponse(repository.save(entity));
    }

    public ExpenseResponse update(Long id, ExpenseRequest request) {
        Expense entity = findEntity(id);
        if (entity.getOccurrenceId() != null) throw new IllegalArgumentException("Este pago procede de un vencimiento. Para corregirlo, archívalo y registra el pago correcto desde Recurrencias.");
        if (entity.getUtilityBillId() != null) throw new IllegalArgumentException("Este gasto pertenece a un recibo y su importe es fijo. Archívalo para corregir el recibo desde Suministros.");
        entity.setProperty(request.getPropertyId() != null ? propertyService.findEntity(request.getPropertyId()) : null);
        entity.setAmount(request.getAmount());
        entity.setDescription(request.getDescription());
        entity.setExpenseDate(request.getExpenseDate());
        entity.setCategory(request.getCategory());
        return toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        Expense entity = findEntity(id);
        if (entity.getUtilityBillId() != null) utilityBills.lockById(entity.getUtilityBillId()).orElseThrow();
        paymentIntegrity.lock(entity.getOccurrenceId(), null, false);
        entity.setArchived(true);
        repository.save(entity);
    }

    public BigDecimal total() {
        if (securityUtil.isAdmin()) {
            return repository.sumAllGlobal();
        }
        return repository.sumAll(securityUtil.getCurrentUserId());
    }

    public BigDecimal total(LocalDate from, LocalDate to) {
        DateRangeValidator.validate(from, to);
        if (from == null && to == null) return total();
        return repository.sumForPeriod(securityUtil.isAdmin() ? null : securityUtil.getCurrentUserId(), from, to);
    }

    public void restore(Long id) {
        var entity = findEntity(id);
        if (entity.getUtilityBillId() != null && utilityBills.lockById(entity.getUtilityBillId()).orElseThrow().isArchived()) throw new IllegalArgumentException("Restaura primero el recibo de suministros");
        if (entity.isArchived()) paymentIntegrity.lock(entity.getOccurrenceId(), entity.getAmount(), false);
        entity.setArchived(false);
        repository.save(entity);
    }

    public Expense findEntity(Long id) {
        Expense entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));
        if (!securityUtil.isAdmin() && !entity.getUser().getId().equals(securityUtil.getCurrentUserId())) {
            throw new ResourceNotFoundException("Expense", id);
        }
        return entity;
    }

    private ExpenseResponse toResponse(Expense entity) {
        return ExpenseResponse.builder()
                .id(entity.getId())
                .archived(entity.isArchived())
                .occurrenceId(entity.getOccurrenceId())
                .propertyId(entity.getProperty() != null ? entity.getProperty().getId() : null)
                .propertyName(entity.getProperty() != null ? entity.getProperty().getName() : null)
                .amount(entity.getAmount())
                .description(entity.getDescription())
                .expenseDate(entity.getExpenseDate())
                .category(entity.getCategory())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
