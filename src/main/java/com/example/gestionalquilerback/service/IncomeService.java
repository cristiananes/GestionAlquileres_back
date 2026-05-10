package com.example.gestionalquilerback.service;

import com.example.gestionalquilerback.dto.IncomeRequest;
import com.example.gestionalquilerback.dto.IncomeResponse;
import com.example.gestionalquilerback.exception.ResourceNotFoundException;
import com.example.gestionalquilerback.model.entity.Income;
import com.example.gestionalquilerback.model.entity.Property;
import com.example.gestionalquilerback.repository.IncomeRepository;
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

    public List<IncomeResponse> findAll(Long propertyId, LocalDate from, LocalDate to) {
        if (propertyId != null) {
            return repository.findByPropertyId(propertyId).stream().map(this::toResponse).toList();
        }
        if (from != null && to != null) {
            return repository.findByIncomeDateBetween(from, to).stream().map(this::toResponse).toList();
        }
        return repository.findAll().stream().map(this::toResponse).toList();
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
        if (!repository.existsById(id)) throw new ResourceNotFoundException("Income", id);
        repository.deleteById(id);
    }

    public BigDecimal total() {
        return repository.sumAll();
    }

    public Income findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Income", id));
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
