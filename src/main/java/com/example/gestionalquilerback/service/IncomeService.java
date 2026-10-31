package com.example.gestionalquilerback.service;

import com.example.gestionalquilerback.dto.IncomeRequest;
import com.example.gestionalquilerback.dto.IncomeResponse;
import com.example.gestionalquilerback.exception.ResourceNotFoundException;
import com.example.gestionalquilerback.model.entity.Income;
import com.example.gestionalquilerback.repository.IncomeRepository;
import com.example.gestionalquilerback.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@org.springframework.transaction.annotation.Transactional
@RequiredArgsConstructor
public class IncomeService {

    private final IncomeRepository repository;
    private final PropertyService propertyService;
    private final SecurityUtil securityUtil;
    private final PaymentIntegrity paymentIntegrity;
    private final UtilityIntegrity utilityIntegrity;

    public List<IncomeResponse> findAll(Long propertyId, LocalDate from, LocalDate to) {
        return findAll(propertyId, from, to, false);
    }

    public List<IncomeResponse> findAll(Long propertyId, LocalDate from, LocalDate to, boolean archived) {
        DateRangeValidator.validate(from, to);
        List<Income> incomes;
        if (securityUtil.isAdmin()) {
            incomes = repository.findAll();
        } else {
            incomes = repository.findByUserId(securityUtil.getCurrentUserId());
        }
        return incomes.stream()
                .filter(e -> e.isArchived() == archived)
                .filter(i -> propertyId == null || (i.getProperty() != null && i.getProperty().getId().equals(propertyId)))
                .filter(i -> from == null || !i.getIncomeDate().isBefore(from))
                .filter(i -> to == null || !i.getIncomeDate().isAfter(to))
                .map(this::toResponse)
                .toList();
    }

    public IncomeResponse findById(Long id) {
        return toResponse(findEntity(id));
    }

    public IncomeResponse create(IncomeRequest request) {
        if (request.getIncomeDate().isAfter(LocalDate.now())) throw new IllegalArgumentException("Para fechas futuras usa un vencimiento previsto en Recurrencias");
        if (request.getClientRequestId() != null) {
            var existing = repository.findByClientRequestIdAndUserId(request.getClientRequestId(), securityUtil.getCurrentUserId());
            if (existing.isPresent()) return toResponse(existing.get());
        }
        Income entity = Income.builder()
                .clientRequestId(request.getClientRequestId())
                .property(request.getPropertyId() != null ? propertyService.findEntity(request.getPropertyId()) : null)
                .amount(request.getAmount())
                .description(request.getDescription())
                .incomeDate(request.getIncomeDate())
                .incomeType(request.getIncomeType())
                .paymentMethod(request.getPaymentMethod())
                .user(securityUtil.getCurrentUser())
                .build();
        return toResponse(repository.save(entity));
    }

    public IncomeResponse update(Long id, IncomeRequest request) {
        Income entity = findEntity(id);
        if (entity.getOccurrenceId() != null) throw new IllegalArgumentException("Este pago procede de un vencimiento. Para corregirlo, archívalo y registra el pago correcto desde Recurrencias.");
        if (entity.getUtilityShareId() != null) throw new IllegalArgumentException("Este reembolso pertenece a un recibo. Archívalo y registra el correcto desde Suministros.");
        entity.setProperty(request.getPropertyId() != null ? propertyService.findEntity(request.getPropertyId()) : null);
        entity.setAmount(request.getAmount());
        entity.setDescription(request.getDescription());
        entity.setIncomeDate(request.getIncomeDate());
        entity.setIncomeType(request.getIncomeType());
        entity.setPaymentMethod(request.getPaymentMethod());
        return toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        Income entity = findEntity(id);
        utilityIntegrity.lock(entity.getUtilityShareId(), entity.getId(), null);
        paymentIntegrity.lock(entity.getOccurrenceId(), null, true);
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
        if (entity.isArchived()) utilityIntegrity.lock(entity.getUtilityShareId(), entity.getId(), entity.getAmount());
        if (entity.isArchived()) paymentIntegrity.lock(entity.getOccurrenceId(), entity.getAmount(), true);
        entity.setArchived(false);
        repository.save(entity);
    }

    public Income findEntity(Long id) {
        Income entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Income", id));
        if (!securityUtil.isAdmin() && !entity.getUser().getId().equals(securityUtil.getCurrentUserId())) {
            throw new ResourceNotFoundException("Income", id);
        }
        return entity;
    }

    private IncomeResponse toResponse(Income entity) {
        return IncomeResponse.builder()
                .id(entity.getId())
                .archived(entity.isArchived())
                .occurrenceId(entity.getOccurrenceId())
                .utilityShareId(entity.getUtilityShareId())
                .propertyId(entity.getProperty() != null ? entity.getProperty().getId() : null)
                .propertyName(entity.getProperty() != null ? entity.getProperty().getName() : null)
                .amount(entity.getAmount())
                .description(entity.getDescription())
                .incomeDate(entity.getIncomeDate())
                .incomeType(entity.getIncomeType())
                .paymentMethod(entity.getPaymentMethod())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
