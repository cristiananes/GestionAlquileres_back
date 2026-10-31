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
import java.time.*;
import java.util.*;
@Service @RequiredArgsConstructor @Transactional
public class LeaseService {
    private final LeaseRepository leases;
    private final PropertyService properties;
    private final RecurrenceService recurrences;
    private final RecurringRuleRepository rules;
    private final CalendarEventRepository events;
    private final SecurityUtil security;
    private final OccupancyService occupancy;
    public List<LeaseResponse> list(Long propertyId, boolean archived) {
        properties.findEntity(propertyId);
        return leases.findByPropertyId(propertyId).stream().filter(l -> l.isArchived() == archived).map(this::response).toList();
    }
    public LeaseResponse create(Long propertyId, LeaseRequest req) {
        occupancy.lock(propertyId);
        var p = properties.findEntity(propertyId);
        if (p.isArchived()) throw new IllegalArgumentException("Restaura la propiedad antes de añadir contratos");
        DateRangeValidator.validate(req.getStartDate(), req.getEndDate());
        var duplicate = leases.findByClientRequestIdAndUserId(req.getClientRequestId(), security.getCurrentUserId());
        if (duplicate.isPresent()) return response(duplicate.get());
        var lease = Lease.builder().property(p).user(security.getCurrentUser()).tenantName(req.getTenantName()).tenantEmail(req.getTenantEmail()).tenantPhone(req.getTenantPhone())
                .startDate(req.getStartDate()).endDate(req.getEndDate()).monthlyRent(req.getMonthlyRent()).depositAmount(req.getDepositAmount()).paymentDay(req.getPaymentDay()).clientRequestId(req.getClientRequestId()).build();
        occupancy.assign(lease,req.getRentalScope(),req.getRoomId(),req.getOccupancyFrom(),req.getOccupancyTo());
        if (req.isGenerateRecurrence()) {
            var month = YearMonth.from(req.getStartDate());
            var anchor = month.atDay(Math.min(req.getPaymentDay(), month.lengthOfMonth()));
            if (anchor.isBefore(req.getStartDate())) { month = month.plusMonths(1); anchor = month.atDay(Math.min(req.getPaymentDay(), month.lengthOfMonth())); }
            var canonicalAnchor = anchor;
            if (req.getEndDate() == null || !canonicalAnchor.isAfter(req.getEndDate())) {
                var result = recurrences.create(RecurringRuleRequest.builder().propertyId(propertyId).kind(MovementKind.INCOME).frequency(RecurrenceFrequency.MONTHLY)
                        .amount(req.getMonthlyRent()).description("Alquiler · " + req.getTenantName()).startDate(canonicalAnchor).endDate(req.getEndDate())
                        .incomeType(IncomeType.RENT).paymentMethod(PaymentMethod.TRANSFER).clientRequestId("lease:" + req.getClientRequestId()).build());
                var rule = rules.findById(result.id()).orElseThrow();
                rule.setDayOfMonth(req.getPaymentDay());
                rules.save(rule);
                lease.setRecurringRule(rule);
            }
        }
        if (req.getEndDate() != null) lease.setEndEvent(events.save(CalendarEvent.builder().property(p).user(security.getCurrentUser()).title("Fin del contrato · " + req.getTenantName())
                .startDateTime(req.getEndDate().atStartOfDay()).endDateTime(req.getEndDate().atTime(23,59)).allDay(true).eventType(EventType.CONTRACT_END).color("#8b5cf6")
                .clientRequestId("lease:" + req.getClientRequestId()).build()));
        return response(leases.save(lease));
    }
    public void archive(Long id, boolean archived) {
        var lease = leases.findById(id).orElseThrow(() -> new ResourceNotFoundException("Lease", id));
        if (!security.isAdmin() && !lease.getUser().getId().equals(security.getCurrentUserId())) throw new ResourceNotFoundException("Lease", id);
        occupancy.lock(lease.getProperty().getId());
        if (!archived) occupancy.check(lease);
        lease.setArchived(archived);
        if (lease.getRecurringRule() != null) recurrences.archive(lease.getRecurringRule().getId(), archived);
        if (lease.getEndEvent() != null) { lease.getEndEvent().setArchived(archived); events.save(lease.getEndEvent()); }
        leases.save(lease);
    }
    public LeaseResponse assign(Long id,OccupancyRequest req) {
        var l=leases.findById(id).orElseThrow(()->new ResourceNotFoundException("Lease",id));properties.findEntity(l.getProperty().getId());
        occupancy.assign(l,req.rentalScope(),req.roomId(),req.from(),req.to());return response(leases.save(l));
    }
    private LeaseResponse response(Lease l) { return new LeaseResponse(l.getId(), l.getProperty().getId(), l.getTenantName(), l.getTenantEmail(), l.getTenantPhone(), l.getStartDate(), l.getEndDate(), l.getMonthlyRent(), l.getDepositAmount(), l.getPaymentDay(), l.getRecurringRule() == null ? null : l.getRecurringRule().getId(), l.isArchived(), l.getRoom()==null?null:l.getRoom().getId(), l.getRoom()==null?null:l.getRoom().getName(), l.getRoom()==null?null:l.getRoom().getNumber(), l.getRentalScope()==null?RentalScope.UNASSIGNED:l.getRentalScope(), l.getOccupancyFrom()==null?l.getStartDate():l.getOccupancyFrom(), l.getOccupancyTo()==null?l.getEndDate():l.getOccupancyTo()); }
}
