package com.example.gestionalquilerback.model.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.math.BigDecimal;
@Entity @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Lease {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private Property property;
    @ManyToOne(optional = false) private User user;
    @ManyToOne(fetch=FetchType.LAZY) private Room room;
    @Enumerated(EnumType.STRING) private com.example.gestionalquilerback.model.enums.RentalScope rentalScope;
    private LocalDate occupancyFrom;
    private LocalDate occupancyTo;
    @Column(nullable = false) private String tenantName;
    private String tenantEmail;
    private String tenantPhone;
    @Column(nullable = false) private LocalDate startDate;
    private LocalDate endDate;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal monthlyRent;
    @Column(precision = 12, scale = 2) private BigDecimal depositAmount;
    @Column(nullable = false) private int paymentDay;
    @ManyToOne private RecurringRule recurringRule;
    @ManyToOne private CalendarEvent endEvent;
    @Column(unique = true, length = 80) private String clientRequestId;
    @Column(nullable = false, columnDefinition = "boolean default false") private boolean archived;
}
