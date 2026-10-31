package com.example.gestionalquilerback.model.entity;
import jakarta.persistence.*;import lombok.*;import java.math.BigDecimal;import java.time.LocalDate;
@Entity @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SettlementLine {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) private OwnerSettlement settlement;
 @Column(unique=true,length=60) private String activeKey;
 @Column(nullable=false,length=10) private String kind;
 @Column(nullable=false) private Long movementId;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal amount;
 @Column(nullable=false) private LocalDate movementDate;
 @Column(nullable=false) private String description;
}
