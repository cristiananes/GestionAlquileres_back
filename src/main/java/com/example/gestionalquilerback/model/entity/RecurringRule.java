package com.example.gestionalquilerback.model.entity;
import com.example.gestionalquilerback.model.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecurringRule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private User user;
    @ManyToOne private Property property;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private MovementKind kind;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private RecurrenceFrequency frequency;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal amount;
    @Column(nullable = false) private String description;
    @Column(nullable = false) private LocalDate startDate;
    private LocalDate endDate;
    private Integer dayOfMonth;
    @Enumerated(EnumType.STRING) private IncomeType incomeType;
    @Enumerated(EnumType.STRING) private PaymentMethod paymentMethod;
    @Enumerated(EnumType.STRING) private ExpenseCategory category;
    @Column(unique = true, length = 80) private String clientRequestId;
    @Column(nullable = false, columnDefinition = "boolean default false") private boolean archived;
    @Column(nullable = false) private LocalDateTime createdAt;
    @PrePersist void created() { createdAt = LocalDateTime.now(); }
}
