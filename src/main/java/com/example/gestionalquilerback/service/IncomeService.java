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
@RequiredArgsConstructor
public class IncomeService {

    private final IncomeRepository repository;
    private final PropertyService propertyService;
    private final SecurityUtil securityUtil;

    public List<IncomeResponse> findAll(Long propertyId, LocalDate from, LocalDate to) {
        List<Income> incomes;
        if (securityUtil.isAdmin()) {
            incomes = repository.findAll();
        } else {
            incomes = repository.findByUserId(securityUtil.getCurrentUserId());
        }
        return incomes.stream()
                .filter(i -> propertyId == null || (i.getProperty() != null && i.getProperty().getId().equals(propertyId)))
                .filter(i -> from == null || to == null || (!i.getIncomeDate().isBefore(from) && !i.getIncomeDate().isAfter(to)))
                .map(this::toResponse)
                .toList();
    }

    public IncomeResponse findById(Long id) {
        return toResponse(findEntity(id));
    }

    public IncomeResponse create(IncomeRequest request) {
        Income entity = Income.builder()
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
        repository.deleteById(entity.getId());
    }

    public BigDecimal total() {
        if (securityUtil.isAdmin()) {
            return repository.sumAllGlobal();
        }
        return repository.sumAll(securityUtil.getCurrentUserId());
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
