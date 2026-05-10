package com.example.gestionalquilerback.service;

import com.example.gestionalquilerback.dto.ExpenseRequest;
import com.example.gestionalquilerback.dto.ExpenseResponse;
import com.example.gestionalquilerback.exception.ResourceNotFoundException;
import com.example.gestionalquilerback.model.entity.Expense;
import com.example.gestionalquilerback.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository repository;
    private final PropertyService propertyService;

    public List<ExpenseResponse> findAll(Long propertyId, LocalDate from, LocalDate to) {
        if (propertyId != null) {
            return repository.findByPropertyId(propertyId).stream().map(this::toResponse).toList();
        }
        if (from != null && to != null) {
            return repository.findByExpenseDateBetween(from, to).stream().map(this::toResponse).toList();
        }
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public ExpenseResponse findById(Long id) {
        return toResponse(findEntity(id));
    }

    public ExpenseResponse create(ExpenseRequest request) {
        Expense entity = Expense.builder()
                .property(request.getPropertyId() != null ? propertyService.findEntity(request.getPropertyId()) : null)
                .amount(request.getAmount())
                .description(request.getDescription())
                .expenseDate(request.getExpenseDate())
                .category(request.getCategory())
                .build();
        return toResponse(repository.save(entity));
    }

    public ExpenseResponse update(Long id, ExpenseRequest request) {
        Expense entity = findEntity(id);
        entity.setProperty(request.getPropertyId() != null ? propertyService.findEntity(request.getPropertyId()) : null);
        entity.setAmount(request.getAmount());
        entity.setDescription(request.getDescription());
        entity.setExpenseDate(request.getExpenseDate());
        entity.setCategory(request.getCategory());
        return toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("Expense", id);
        repository.deleteById(id);
    }

    public BigDecimal total() {
        return repository.sumAll();
    }

    public Expense findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));
    }

    private ExpenseResponse toResponse(Expense entity) {
        return ExpenseResponse.builder()
                .id(entity.getId())
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
