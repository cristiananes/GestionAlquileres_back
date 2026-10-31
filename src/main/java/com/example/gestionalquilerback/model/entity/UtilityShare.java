package com.example.gestionalquilerback.model.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Entity @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UtilityShare {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) private UtilityBill bill;
 @Column(nullable=false,length=120) private String name;
 @Column(length=120) private String room;
 @Column(length=180) private String email;
 @Column(length=40) private String phone;
 private Integer roomNumber;
 private LocalDate stayFrom;
 private LocalDate stayTo;
 @Column(precision=7,scale=4) private BigDecimal percent;
 @Column(nullable=false) private boolean owner;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal amount;
}
