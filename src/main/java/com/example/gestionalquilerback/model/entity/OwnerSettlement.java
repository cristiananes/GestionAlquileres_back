package com.example.gestionalquilerback.model.entity;
import jakarta.persistence.*;import lombok.*;import java.math.BigDecimal;import java.time.LocalDate;
import com.example.gestionalquilerback.dto.ManagementRequest.*;
@Entity @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class OwnerSettlement {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) private Property property;
 @ManyToOne(optional=false) private User user;
 @Column(nullable=false,unique=true,length=64) private String requestKey;
 @Column(nullable=false,length=64) private String requestHash;
 @Column(nullable=false) private LocalDate periodFrom;
 @Column(nullable=false) private LocalDate periodTo;
 @Column(nullable=false,length=160) private String ownerName;
 @Column(length=180) private String ownerEmail;
 @Enumerated(EnumType.STRING) private FeeMode feeMode;
 @Column(precision=12,scale=2) private BigDecimal feeRate;
 @Column(precision=5,scale=2) private BigDecimal taxRate;
 @Enumerated(EnumType.STRING) private CollectionMode collectionMode;
 @Column(nullable=false,precision=14,scale=2) private BigDecimal totalIncome;
 @Column(nullable=false,precision=14,scale=2) private BigDecimal totalExpense;
 @Column(nullable=false,precision=14,scale=2) private BigDecimal feeBase;
 @Column(nullable=false,precision=14,scale=2) private BigDecimal feeNet;
 @Column(nullable=false,precision=14,scale=2) private BigDecimal feeTax;
 @Column(nullable=false,precision=14,scale=2) private BigDecimal netToOwner;
 @Column(nullable=false,length=20) private String status;
 private LocalDate paidDate;
}
