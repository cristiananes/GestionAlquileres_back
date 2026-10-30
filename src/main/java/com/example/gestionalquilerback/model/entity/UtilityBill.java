package com.example.gestionalquilerback.model.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.example.gestionalquilerback.dto.UtilityBillRequest.*;
@Entity @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UtilityBill {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) private Property property;
 @ManyToOne(optional=false) private User user;
 @Column(nullable=false,length=80,unique=true) private String clientRequestId;
 @Column(nullable=false,length=64,unique=true) private String fingerprint;
 @Column(nullable=false,length=64) private String requestHash;
 @Column(nullable=false,length=30) private String supply;
 @Column(nullable=false,length=120) private String provider;
 @Column(nullable=false,length=100) private String reference;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal amount;
 @Column(nullable=false) private LocalDate periodFrom;
 @Column(nullable=false) private LocalDate periodTo;
 @Column(nullable=false) private LocalDate dueDate;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private Method method;
 private int roomCount;
 @Enumerated(EnumType.STRING) private Vacancy vacancy;
 @ManyToOne private PropertyDocument document;
 @Column(nullable=false) private boolean archived;
}
