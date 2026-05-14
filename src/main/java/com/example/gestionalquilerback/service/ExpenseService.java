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
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository repository;
    private final PropertyService propertyService;
    private final SecurityUtil securityUtil;

    public List<ExpenseResponse> findAll(Long propertyId, LocalDate from, LocalDate to) {
        List<Expense> expenses;
        if (securityUtil.isAdmin()) {
            expenses = repository.findAll();
        } else {
            expenses = repository.findByUserId(securityUtil.getCurrentUserId());
        }
        return expenses.stream()
                .filter(e -> propertyId == null || (e.getProperty() != null && e.getProperty().getId().equals(propertyId)))
                .filter(e -> from == null || to == null || (!e.getExpenseDate().isBefore(from) && !e.getExpenseDate().isAfter(to)))
                .map(this::toResponse)
                .toList();
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
                .user(securityUtil.getCurrentUser())
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
        Expense entity = findEntity(id);
        repository.deleteById(entity.getId());
    }

    public BigDecimal total() {
        if (securityUtil.isAdmin()) {
            return repository.sumAllGlobal();
        }
        return repository.sumAll(securityUtil.getCurrentUserId());
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
