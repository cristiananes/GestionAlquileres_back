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
import java.util.*;
import java.math.BigDecimal;
import java.time.*;
@Service @RequiredArgsConstructor @Transactional
public class RecurrenceService {
    private final RecurringRuleRepository rules;
    private final PaymentOccurrenceRepository occurrences;
    private final IncomeRepository incomes;
    private final ExpenseRepository expenses;
    private final PropertyService properties;
    private final SecurityUtil security;

    public List<RecurringRuleResponse> findAll(Long propertyId, boolean archived) {
        return visible().stream().filter(r -> r.isArchived() == archived)
                .filter(r -> propertyId == null || r.getProperty() != null && r.getProperty().getId().equals(propertyId))
                .map(this::response).toList();
    }
    private List<RecurringRule> visible() { return security.isAdmin() ? rules.findAll() : rules.findByUserId(security.getCurrentUserId()); }
    public RecurringRuleResponse create(RecurringRuleRequest req) {
        DateRangeValidator.validate(req.getStartDate(), req.getEndDate());
        if (req.getKind() == MovementKind.INCOME && req.getIncomeType() == null || req.getKind() == MovementKind.EXPENSE && req.getCategory() == null)
            throw new IllegalArgumentException("Indica el tipo de ingreso o categoría del gasto");
        var existing = rules.findByClientRequestIdAndUserId(req.getClientRequestId(), security.getCurrentUserId());
        if (existing.isPresent()) return response(existing.get());
        var property = req.getPropertyId() == null ? null : properties.findEntity(req.getPropertyId());
        if (property != null && property.isArchived()) throw new IllegalArgumentException("Restaura la propiedad antes de añadir recurrencias");
        return response(rules.save(RecurringRule.builder().user(security.getCurrentUser()).property(property).kind(req.getKind())
                .frequency(req.getFrequency()).amount(req.getAmount()).description(req.getDescription()).startDate(req.getStartDate())
                .endDate(req.getEndDate()).incomeType(req.getIncomeType()).paymentMethod(req.getPaymentMethod()).category(req.getCategory())
                .clientRequestId(req.getClientRequestId()).build()));
    }
    private RecurringRule authorized(Long id, boolean locked) {
        var rule = (locked ? rules.lockById(id) : rules.findById(id)).orElseThrow(() -> new ResourceNotFoundException("RecurringRule", id));
        if (!security.isAdmin() && !rule.getUser().getId().equals(security.getCurrentUserId())) throw new ResourceNotFoundException("RecurringRule", id);
        return rule;
    }
    public void archive(Long id, boolean archived) { var rule = authorized(id, true); rule.setArchived(archived); rules.save(rule); }
    public void finish(Long id) {
        var rule = authorized(id, true);
        var end = LocalDate.now();
        if (rule.getEndDate() == null || rule.getEndDate().isAfter(end)) rule.setEndDate(end);
        rules.save(rule);
    }
    public List<OccurrenceResponse> upcoming(Long propertyId, LocalDate from, LocalDate to) {
        var result = new ArrayList<OccurrenceResponse>();
        for (var rule : visible()) {
            if (rule.isArchived() || rule.getProperty() != null && rule.getProperty().isArchived()) continue;
            if (propertyId != null && (rule.getProperty() == null || !rule.getProperty().getId().equals(propertyId))) continue;
            var confirmed = new HashMap<LocalDate, PaymentOccurrence>();
            occurrences.findByRuleId(rule.getId()).forEach(o -> confirmed.put(o.getDueDate(), o));
            var dates = new TreeSet<>(RecurrenceDates.between(rule.getStartDate(), rule.getEndDate(), rule.getFrequency(), from, to, rule.getDayOfMonth()));
            // Preserve confirmed occurrences even if a recurrence has subsequently ended.
            confirmed.keySet().stream().filter(d -> !d.isBefore(from) && !d.isAfter(to)).forEach(dates::add);
            for (var due : dates) result.add(occurrence(rule, due, confirmed.get(due)));
        }
        return result.stream().sorted(Comparator.comparing(OccurrenceResponse::dueDate).thenComparing(OccurrenceResponse::ruleId)).toList();
    }
    private BigDecimal paid(RecurringRule rule, PaymentOccurrence occurrence) {
        if (occurrence == null) return BigDecimal.ZERO;
        if (rule.getKind() == MovementKind.INCOME) return incomes.findByOccurrenceIdAndArchivedFalse(occurrence.getId()).stream().map(Income::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return expenses.findByOccurrenceIdAndArchivedFalse(occurrence.getId()).stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    private OccurrenceResponse occurrence(RecurringRule rule, LocalDate due, PaymentOccurrence occurrence) {
        var amount = occurrence == null ? rule.getAmount() : occurrence.getAmountDue();
        var paid = paid(rule, occurrence);
        var remaining = amount.subtract(paid);
        var status = remaining.signum() <= 0 ? "PAID" : paid.signum() > 0 ? "PARTIAL" : due.isBefore(LocalDate.now()) ? "OVERDUE" : "PENDING";
        return new OccurrenceResponse(rule.getId(), rule.getProperty() == null ? null : rule.getProperty().getId(), rule.getProperty() == null ? null : rule.getProperty().getName(), rule.getKind(), rule.getDescription(), due, amount, paid, remaining, status);
    }
    public OccurrenceResponse pay(Long id, PaymentRequest req) {
        var rule = authorized(id, true); // Serializes confirmations, including the first one, across application instances.
        if (rule.isArchived()) throw new IllegalArgumentException("Restaura la recurrencia antes de confirmar pagos");
        if (req.getPaymentDate().isAfter(LocalDate.now())) throw new IllegalArgumentException("Un pago realizado no puede tener fecha futura");
        if (!RecurrenceDates.between(rule.getStartDate(), rule.getEndDate(), rule.getFrequency(), req.getDueDate(), req.getDueDate(), rule.getDayOfMonth()).contains(req.getDueDate()))
            throw new IllegalArgumentException("La fecha no corresponde a un vencimiento de esta recurrencia");
        var occurrence = occurrences.findByRuleIdAndDueDate(id, req.getDueDate()).orElseGet(() -> occurrences.saveAndFlush(PaymentOccurrence.builder().rule(rule).dueDate(req.getDueDate()).amountDue(rule.getAmount()).build()));
        var duplicateIncome = incomes.findByClientRequestIdAndUserId(req.getClientRequestId(), rule.getUser().getId());
        var duplicateExpense = expenses.findByClientRequestIdAndUserId(req.getClientRequestId(), rule.getUser().getId());
        if (duplicateIncome.isPresent() || duplicateExpense.isPresent()) {
            Long linked = duplicateIncome.isPresent() ? duplicateIncome.get().getOccurrenceId() : duplicateExpense.get().getOccurrenceId();
            if (!occurrence.getId().equals(linked)) throw new IllegalArgumentException("La operación ya se utilizó para otro vencimiento");
            var duplicateAmount = duplicateIncome.isPresent() ? duplicateIncome.get().getAmount() : duplicateExpense.get().getAmount();
            var duplicateDate = duplicateIncome.isPresent() ? duplicateIncome.get().getIncomeDate() : duplicateExpense.get().getExpenseDate();
            var archived = duplicateIncome.isPresent() ? duplicateIncome.get().isArchived() : duplicateExpense.get().isArchived();
            if (archived || duplicateAmount.compareTo(req.getAmount()) != 0 || !duplicateDate.equals(req.getPaymentDate()))
                throw new IllegalArgumentException("Esta operación ya se registró con otros datos o está archivada. Revisa los movimientos antes de registrar un nuevo pago.");
            return occurrence(rule, req.getDueDate(), occurrence);
        }
        if (req.getAmount().compareTo(occurrence.getAmountDue().subtract(paid(rule, occurrence))) > 0) throw new IllegalArgumentException("El importe supera lo pendiente");
        if (rule.getKind() == MovementKind.INCOME) incomes.saveAndFlush(Income.builder().user(rule.getUser()).property(rule.getProperty()).amount(req.getAmount()).description(rule.getDescription())
                .incomeDate(req.getPaymentDate()).incomeType(rule.getIncomeType()).paymentMethod(rule.getPaymentMethod()).occurrenceId(occurrence.getId()).clientRequestId(req.getClientRequestId()).build());
        else expenses.saveAndFlush(Expense.builder().user(rule.getUser()).property(rule.getProperty()).amount(req.getAmount()).description(rule.getDescription())
                .expenseDate(req.getPaymentDate()).category(rule.getCategory()).occurrenceId(occurrence.getId()).clientRequestId(req.getClientRequestId()).build());
        return occurrence(rule, req.getDueDate(), occurrence);
    }
    public RecurringRuleResponse response(RecurringRule r) {
        return new RecurringRuleResponse(r.getId(), r.getProperty() == null ? null : r.getProperty().getId(), r.getProperty() == null ? null : r.getProperty().getName(), r.getKind(), r.getFrequency(), r.getAmount(), r.getDescription(), r.getStartDate(), r.getEndDate(), r.getIncomeType(), r.getPaymentMethod(), r.getCategory(), r.isArchived());
    }
}
